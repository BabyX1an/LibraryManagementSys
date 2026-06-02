import socket
import threading
import time
import requests
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routers import api

# Nacos心跳线程
heartbeat_thread = None
heartbeat_stop_event = threading.Event()


def get_local_ip():
    """获取本机局域网IP"""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"


def register_to_nacos():
    """通过HTTP API注册服务到Nacos"""
    ip = get_local_ip()
    try:
        # 先注册实例（ephemeral模式，需要定期发送心跳）
        resp = requests.post(
            f"http://{settings.NACOS_SERVER}/nacos/v1/ns/instance",
            params={
                "serviceName": settings.SERVICE_NAME,
                "ip": ip,
                "port": settings.SERVICE_PORT,
                "groupName": settings.NACOS_GROUP,
                "namespaceId": settings.NACOS_NAMESPACE,
                "weight": 1.0,
                "enabled": "true",
                "healthy": "true",
                "ephemeral": "true",
                "username": settings.NACOS_USERNAME,
                "password": settings.NACOS_PASSWORD,
            },
        )
        if resp.ok:
            print(f"已注册到Nacos: {settings.SERVICE_NAME} -> {ip}:{settings.SERVICE_PORT}")
        else:
            print(f"Nacos注册失败: {resp.status_code} {resp.text}")
    except Exception as e:
        print(f"Nacos注册失败（服务仍可正常运行）: {e}")


def heartbeat_loop():
    """定期发送心跳保活（ephemeral实例需要）"""
    ip = get_local_ip()
    while not heartbeat_stop_event.is_set():
        try:
            resp = requests.put(
                f"http://{settings.NACOS_SERVER}/nacos/v1/ns/instance/beat",
                params={
                    "serviceName": settings.SERVICE_NAME,
                    "ip": ip,
                    "port": settings.SERVICE_PORT,
                    "groupName": settings.NACOS_GROUP,
                    "namespaceId": settings.NACOS_NAMESPACE,
                    "beat": f'{{"ip":"{ip}","port":{settings.SERVICE_PORT},"serviceName":"{settings.SERVICE_NAME}","weight":1.0}}',
                    "ephemeral": "true",
                    "username": settings.NACOS_USERNAME,
                    "password": settings.NACOS_PASSWORD,
                },
            )
            if not resp.ok:
                print(f"Nacos心跳失败: {resp.status_code}")
        except Exception as e:
            print(f"Nacos心跳异常: {e}")
        heartbeat_stop_event.wait(5)


def deregister_from_nacos():
    """从Nacos注销服务"""
    global heartbeat_thread, heartbeat_stop_event
    # 停止心跳
    heartbeat_stop_event.set()
    if heartbeat_thread:
        heartbeat_thread.join(timeout=10)
    # 注销实例
    ip = get_local_ip()
    try:
        requests.delete(
            f"http://{settings.NACOS_SERVER}/nacos/v1/ns/instance",
            params={
                "serviceName": settings.SERVICE_NAME,
                "ip": ip,
                "port": settings.SERVICE_PORT,
                "groupName": settings.NACOS_GROUP,
                "namespaceId": settings.NACOS_NAMESPACE,
                "ephemeral": "true",
                "username": settings.NACOS_USERNAME,
                "password": settings.NACOS_PASSWORD,
            },
        )
        print(f"已从Nacos注销: {settings.SERVICE_NAME}")
    except Exception as e:
        print(f"Nacos注销失败: {e}")


@asynccontextmanager
async def lifespan(app):
    global heartbeat_thread, heartbeat_stop_event

    # 启动时预热：加载BERT模型 + 构建向量缓存
    print("正在预热AI模型...")
    embedding_service = api.get_embedding_service()
    embedding_service.get_or_build_embeddings()
    api.get_recommend_service()
    api.get_search_service()
    print("AI模型预热完成")

    # 注册到Nacos并启动心跳
    register_to_nacos()
    heartbeat_stop_event.clear()
    heartbeat_thread = threading.Thread(target=heartbeat_loop, daemon=True)
    heartbeat_thread.start()

    yield

    # 从Nacos注销
    deregister_from_nacos()


app = FastAPI(
    title="AI Recommendation Service",
    description="智能推荐与语义搜索服务",
    version="1.0.0",
    lifespan=lifespan
)

# CORS配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(api.router, prefix="/api/v1/ai")


@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": settings.SERVICE_NAME}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=settings.SERVICE_PORT)
