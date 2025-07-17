-- Check if pgvector extension exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_extension
        WHERE extname = 'vector'
    ) THEN
        -- Create the pgvector extension
        CREATE EXTENSION IF NOT EXISTS vector;
    END IF;
END
$$;

-- Create the document_chunks table if it doesn't exist
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    chunk_index INTEGER,
    embedding_vector vector(1536)
);

-- Create the documents table if it doesn't exist
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    source_url VARCHAR(1024),
    document_type VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    metadata TEXT
);

-- Create index for vector similarity search
CREATE INDEX IF NOT EXISTS document_chunks_embedding_idx ON document_chunks USING ivfflat (embedding_vector vector_cosine_ops) WITH (lists = 100);

-- Add foreign key constraint if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_document_chunks_document'
    ) THEN
        ALTER TABLE document_chunks
        ADD CONSTRAINT fk_document_chunks_document
        FOREIGN KEY (document_id)
        REFERENCES documents(id)
        ON DELETE CASCADE;
    END IF;
END
$$;