from sqlalchemy import create_engine, Column, BigInteger, String, Text, Integer, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from app.config import settings

# 数据库连接
DATABASE_URL = f"mysql+pymysql://{settings.DB_USER}:{settings.DB_PASSWORD}@{settings.DB_HOST}:{settings.DB_PORT}/{settings.DB_NAME}"
engine = create_engine(DATABASE_URL, pool_pre_ping=True, pool_recycle=3600)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


# 图书实体
class Book(Base):
    __tablename__ = "book"

    id = Column(BigInteger, primary_key=True, index=True)
    title = Column(String(255))
    author = Column(String(100))
    publisher = Column(String(100))
    category = Column(String(50))
    description = Column(Text)
    cover_url = Column(String(500))
    stock = Column(Integer)
    borrowed_count = Column(Integer)
    status = Column(Integer)
    deleted = Column(Integer, default=0)


# 借阅记录实体
class BorrowRecord(Base):
    __tablename__ = "borrow_record"

    id = Column(BigInteger, primary_key=True, index=True)
    user_id = Column(BigInteger, index=True)
    book_id = Column(BigInteger, index=True)
    borrow_time = Column(DateTime)
    due_time = Column(DateTime)
    return_time = Column(DateTime)
    status = Column(Integer)  # 1-借阅中, 2-已归还


# 获取数据库会话
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
