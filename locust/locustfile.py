import random
import string
# import gevent
# from gevent import monkey
# monkey.patch_all()
# import requests
from locust import HttpUser, task, constant


def generate_text(length=8):
    # 영문자 소문자와 숫자 조합으로 username 생성
    characters = string.ascii_lowercase + string.digits
    username = ''.join(random.choice(characters) for _ in range(length))
    return username

# def fetch_notice(client, notice_id, headers):
#     client.get(f"/api/notice/{notice_id}", headers=headers)
#
# def mark_history_read(client, history_id, headers):
#     client.post(f"/api/users/history?historyId={history_id}", headers=headers)
#
# def process_history(client, history, headers):
#     if not history["isRead"]:
#         history_id = history["id"]
#         notice_id = history.get("noticeId")
#         if notice_id and notice_id != 0:
#             fetch_notice(client, notice_id, headers)
#         else:
#             mark_history_read(client, history_id, headers)


class CommonUser(HttpUser):
    host = "http://52.78.109.42:8080"
    wait_time = constant(5)

    @task
    def hello_world(self):
        headers = {"Content-Type": "application/json"}

        # 회원 가입 후 로그인하여 JWT 토큰 생성            -> MySQL
        username = generate_text()
        password = "1q2w3e4r!!"
        res = self.client.post("/api/users/signUp", headers=headers, json={
            "username": username, "password": password, "email": "test@test.com"
        })
        res = self.client.post("/api/users/login", headers=headers, json={
            "username": username,
            "password": password
        })
        token = res.text
        print(f"Generated Token: {token}")
        headers["Authorization"] = "Bearer " + token

        # 게시글(+광고) 리스트 1회 조회 후 게시글 1회 작성(광고도 하나 보고 클릭)
        board_id = 1
        res = self.client.get(f"/api/boards/{board_id}/all-articles", headers=headers)
        articles = res.json()
        res = self.client.get(f"/api/advertisement/all", headers=headers)
        ads = res.json()
        if ads:
            ad_id = random.choice(ads)["id"]
            # 광고 보기
            self.client.get(f"/api/advertisement/{ad_id}", headers=headers)
            self.client.get(f"/api/advertisement/{ad_id}?isTrueView=true", headers=headers)
            # 광고 클릭
            self.client.post(f"/api/advertisement/{ad_id}", headers=headers)
        # 게시글 하나 작성
        self.client.post(f"/api/boards/{board_id}/articles", headers=headers, json={
            "title": generate_text(100),
            "content": generate_text(200)
        })

        if articles:
            article_id = random.choice(articles)["id"]
            # 게시글 조회
            self.client.get(f"/api/boards/{board_id}/articles/{article_id}", headers=headers)
            # 게시글 중에 랜덤하게 하나 댓글 달기
            self.client.post(f"/api/boards/{board_id}/articles/{article_id}/comment", headers=headers, json={
                "content": generate_text(200)
            })

        # 게시글 알림 리스트 1회 조회 후 모든 알림 읽음 처리  -> MongoDB / RabbitMQ
        res = self.client.get(f"/api/users/history", headers=headers)
        history_list = res.json()
        if history_list:
#             jobs = [gevent.spawn(process_history, self.client, history, headers) for history in history_list]
#             gevent.joinall(jobs)

            for history in history_list:
                # 모든 알림 읽음 처리
                if not history["isRead"]:
                    history_id = history["id"]
                    notice_id = history.get("noticeId")
                    if notice_id and notice_id != 0:  # noticeId가 유효한 경우에만 요청
                        self.client.get(f"/api/notice/{notice_id}", headers=headers)
                    else:
                        self.client.post(f"/api/users/history?historyId={history_id}", headers=headers)

        # 게시글 검색(search) 1회 후 인기글 10회 조회     -> ElasticSearch / Redis
        keyword = generate_text(10)
        self.client.post(f"/api/boards/{board_id}/articles/search?keyword={keyword}", headers=headers)

        # 인기글 설정 필요
        # self.client.get(f"/api/boards/{board_id}/articles/1", headers=headers)

        # 공지사항 1분마다 작성                         -> MySQL / MongoDB / RabbitMQ
        self.client.post(f"/api/notice", headers=headers, json={
            "title": generate_text(100),
            "content": generate_text(200)
        })