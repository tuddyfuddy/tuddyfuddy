FROM python:3.12-slim

WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 시스템 패키지 설치
RUN apt-get update && apt-get install -y \
    gcc \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

# Python 패키지 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 환경변수 설정
ENV HOST=0.0.0.0
ENV PORT=8000
ENV WORKERS=4

# 포트 설정
EXPOSE 8000

# Uvicorn으로 실행
CMD ["sh", "-c", "uvicorn app.main:app --host $HOST --port $PORT --workers $WORKERS"]