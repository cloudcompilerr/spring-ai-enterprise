-- Create the pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create the documents table
CREATE TABLE IF NOT EXISTS documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    source_url VARCHAR(1024),
    document_type VARCHAR(50) DEFAULT 'TEXT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

-- Create the document_chunks table
CREATE TABLE IF NOT EXISTS document_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    chunk_index INTEGER DEFAULT 0,
    embedding_vector vector(1536),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_chunks_document 
        FOREIGN KEY (document_id) 
        REFERENCES documents(id) 
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_documents_created_at ON documents(created_at);
CREATE INDEX IF NOT EXISTS idx_documents_type ON documents(document_type);
CREATE INDEX IF NOT EXISTS idx_document_chunks_document_id ON document_chunks(document_id);

-- Create index for vector similarity search (only if we have data)
-- This will be created automatically when first vectors are inserted
-- CREATE INDEX IF NOT EXISTS document_chunks_embedding_idx 
-- ON document_chunks USING ivfflat (embedding_vector vector_cosine_ops) 
-- WITH (lists = 100);

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;