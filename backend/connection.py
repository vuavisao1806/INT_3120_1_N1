from dotenv import load_dotenv
from os import getenv
from psycopg2 import connect
from psycopg2.extras import RealDictCursor

load_dotenv()

def get_connection():
    return connect(
        host=getenv("PG_HOST"),
        port=getenv("PG_PORT"),
        dbname=getenv("PG_DB"),
        user=getenv("PG_USER"),
        password=getenv("PG_PASSWORD"),
        sslmode=getenv("PG_SSLMODE", "require"),
        cursor_factory=RealDictCursor
    )