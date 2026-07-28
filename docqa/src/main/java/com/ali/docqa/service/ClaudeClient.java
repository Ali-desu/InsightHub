package com.ali.docqa.service;

import com.ali.docqa.dto.AskResponse;
import com.ali.docqa.dto.Citation;
import com.ali.docqa.dto.RetrievedChunk;
import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.CitationCharLocation;
import com.anthropic.models.messages.CitationsConfigParam;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.PlainTextSource;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.TextCitation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a grounded, cited answer from a question + the chunks retrieved for it.
 *
 * How citations work here:
 *   - Each retrieved chunk is sent as a "document" content block with citations enabled.
 *   - Claude answers using those documents and tags each claim with the DOCUMENT INDEX it used
 *     (0 = first document we sent, 1 = second, ...).
 *   - Because we send the documents in the same order as `sources`, that index maps straight back
 *     to the chunk's real (documentId, chunkIndex) — the provenance we've carried since ingestion.
 */
@Service
public class ClaudeClient {

    private static final String SYSTEM_PROMPT =
            "You are a document question-answering assistant. Answer the user's question using ONLY " +
            "the provided documents. Cite the documents you rely on. If the answer is not contained " +
            "in the documents, say you don't know rather than guessing.";

    private final AnthropicClient client;
    private final String model;
    private final long maxTokens;

    public ClaudeClient(AnthropicClient client,
                        @Value("${anthropic.model}") String model,
                        @Value("${anthropic.max-tokens}") long maxTokens) {
        this.client = client;
        this.model = model;
        this.maxTokens = maxTokens;
    }

    public AskResponse answer(String question, List<RetrievedChunk> sources) {
        // 1. Each chunk -> a citable "document" block. Order matters: index i here == documentIndex
        //    Claude will report in its citations.
        List<ContentBlockParam> blocks = new ArrayList<>();
        for (RetrievedChunk src : sources) {
            DocumentBlockParam document = DocumentBlockParam.builder()
                    .source(PlainTextSource.builder().data(src.content()).build())
                    .title("doc " + src.documentId() + " · chunk " + src.chunkIndex())
                    .citations(CitationsConfigParam.builder().enabled(true).build())
                    .build();
            blocks.add(ContentBlockParam.ofDocument(document));
        }
        // 2. The question itself, as a normal text block after the documents.
        blocks.add(ContentBlockParam.ofText(TextBlockParam.builder().text(question).build()));

        // 3. One Messages API call.
        MessageCreateParams params = MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(SYSTEM_PROMPT)
                .addUserMessageOfBlockParams(blocks)
                .build();

        Message response = client.messages().create(params);

        // 4. Reassemble the answer text and resolve each citation's documentIndex back to its chunk.
        StringBuilder answer = new StringBuilder();
        List<Citation> citations = new ArrayList<>();
        for (ContentBlock block : response.content()) {
            block.text().ifPresent(textBlock -> {
                answer.append(textBlock.text());
                textBlock.citations().ifPresent(cites -> {
                    for (TextCitation cite : cites) {
                        if (cite.isCharLocation()) {
                            CitationCharLocation loc = cite.asCharLocation();
                            int idx = (int) loc.documentIndex();
                            if (idx >= 0 && idx < sources.size()) {
                                RetrievedChunk src = sources.get(idx);
                                citations.add(new Citation(src.documentId(), src.chunkIndex(), loc.citedText()));
                            }
                        }
                    }
                });
            });
        }
        return new AskResponse(answer.toString(), citations);
    }
}
