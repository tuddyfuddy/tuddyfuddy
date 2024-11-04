import logging
from logging.handlers import RotatingFileHandler

# 색상 코드
RESET = "\033[0m"
GREEN = "\033[32m"
YELLOW = "\033[33m"
RED = "\033[31m"
BOLD = "\033[1m"


class ColoredFormatter(logging.Formatter):
    COLORS = {
        "DEBUG": BOLD + GREEN,
        "INFO": BOLD + GREEN,
        "WARNING": BOLD + YELLOW,
        "ERROR": BOLD + RED,
        "CRITICAL": BOLD + RED,
    }

    def format(self, record):
        levelname = record.levelname
        msg = super().format(record)
        if levelname in self.COLORS:
            return msg.replace(levelname, f"{self.COLORS[levelname]}{levelname}{RESET}")
        return msg


class PlainFormatter(logging.Formatter):
    def format(self, record):
        return super().format(record)


def setup_logger(name):
    # 로거 생성
    logger = logging.getLogger(name)
    if logger.hasHandlers():
        return logger
    logger.setLevel(logging.DEBUG)

    # 핸들러 생성 및 설정
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.DEBUG)

    file_handler = RotatingFileHandler(
        "app.log", maxBytes=10 * 1024 * 1024, backupCount=5, encoding="utf-8"
    )
    file_handler.setLevel(logging.DEBUG)

    log_format = "%(asctime)s - %(levelname)s - %(message)s"
    colored_formatter = ColoredFormatter(log_format)
    console_handler.setFormatter(colored_formatter)
    file_handler.setFormatter(PlainFormatter(log_format))

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

    logger.info("--------------------     logger loaded.         --------------------")
    # logger.debug("debug")
    # logger.info("info")
    # logger.warning("warning")
    # logger.error("error")
    # logger.critical("critical")

    return logger
