from fastapi import FastAPI
from users import router as user_router
from pins import router as pin_router
from posts import router as post_router


app = FastAPI()
app.include_router(user_router)
app.include_router(pin_router)
app.include_router(post_router)