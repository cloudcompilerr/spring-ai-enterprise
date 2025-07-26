-- Create the pgvector extension if it doesn't exist
CREATE EXTENSION IF NOT EXISTS vector;

-- Create the documents table if it doesn't exist
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    source_url VARCHAR(1024),
    document_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT
);

-- Create the document_chunks table if it doesn't exist
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    chunk_index INTEGER,
    embedding_vector vector(1536),
    CONSTRAINT fk_document_chunks_document 
        FOREIGN KEY (document_id) 
        REFERENCES documents(id) 
        ON DELETE CASCADE
);

-- Create index for vector similarity search
CREATE INDEX IF NOT EXISTS document_chunks_embedding_idx 
ON document_chunks USING ivfflat (embedding_vector vector_cosine_ops) 
WITH (lists = 100);