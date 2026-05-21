from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 服务配置
    SERVICE_NAME: str = "ai-service"
    SERVICE_PORT: int = 8108

    # 数据库配置（与Java服务共用同一个数据库）
    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_NAME: str = "library_system"
    DB_USER: str = "root"
    DB_PASSWORD: str = "20040523666Ccc"

    # Redis配置
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 1  # 使用不同的DB避免冲突

    # BERT模型配置
    BERT_MODEL: str = "paraphrase-multilingual-MiniLM-L12-v2"
    EMBEDDING_CACHE_KEY: str = "book:embeddings"
    EMBEDDING_DIM: int = 384

    class Config:
        env_file = ".env"


settings = Settings()
