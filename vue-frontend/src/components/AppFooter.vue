<template>
  <footer class="app-footer">
    <div class="footer-utility">
      <div class="footer-utility-inner">
        <div class="utility-links">
          <a href="#">이용 가이드</a>
          <span class="dot">·</span>
          <a href="#">자주 묻는 질문</a>
          <span class="dot">·</span>
          <a href="#">제휴·입점 문의</a>
        </div>
        <router-link to="/events/new" class="utility-cta">행사 주최자이신가요? 행사 등록하기 →</router-link>
      </div>
    </div>

    <div class="footer-main">
      <div class="footer-col brand-col">
        <div class="footer-logo"><img src="@/assets/images/logo/logo.png" alt="갈래" /></div>
        <p>전국의 축제·전시·공연·문화체험을<br />한곳에서 찾고 예약하세요.</p>
        <div class="social-row">
          <a href="#" aria-label="Instagram">IG</a>
          <a href="#" aria-label="YouTube">YT</a>
          <a href="#" aria-label="X">X</a>
        </div>
      </div>

      <div class="footer-col">
        <b>바로가기</b>
        <router-link to="/events">행사 찾기</router-link>
        <router-link to="/reservations">내 예약</router-link>
        <router-link to="/mypage">마이페이지</router-link>
      </div>

      <div class="footer-col">
        <b>고객지원</b>
        <a href="#">공지사항</a>
        <a href="#">고객센터 1544-0000</a>
        <a href="#">평일 09:00 ~ 18:00</a>
      </div>

      <div class="footer-col">
        <b>약관</b>
        <a href="#" class="strong">이용약관</a>
        <a href="#" class="strong">개인정보처리방침</a>
      </div>
    </div>

    <div class="footer-bottom">
      <span>© {{ year }} 갈래(Galrae). All rights reserved.</span>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.app-footer {
  background: var(--color-bg-secondary);
  border-top: 1px solid var(--color-border);
  margin-top: 80px;
}

.footer-utility {
  border-bottom: 1px solid var(--color-border);
}
.footer-utility-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 18px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.utility-links {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
}
.utility-links a:hover {
  color: var(--color-primary);
}
.utility-links .dot {
  color: var(--color-border-hover);
}
.utility-cta {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-primary);
}
.utility-cta:hover {
  text-decoration: underline;
}

.footer-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 32px;
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr 1fr;
  gap: 32px;
}

.footer-col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 13.5px;
}
.footer-col b {
  font-size: 13px;
  color: var(--color-text-primary);
  margin-bottom: 2px;
}
.footer-col a {
  color: var(--color-text-secondary);
}
.footer-col a:hover {
  color: var(--color-primary);
}
.footer-col a.strong {
  color: var(--color-text-primary);
  font-weight: 600;
}

.brand-col p {
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.6;
}
.footer-logo {
  display: flex;
  align-items: center;
}
.footer-logo img {
  height: 20px;
  width: auto;
}
.social-row {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.social-row a {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  display: grid;
  place-items: center;
  font-size: 11px;
  font-weight: 700;
  transition: var(--transition);
}
.social-row a:hover {
  background: var(--color-primary);
  color: #fff;
}

.footer-bottom {
  border-top: 1px solid var(--color-border);
  padding: 18px 24px;
  max-width: 1200px;
  margin: 0 auto;
  font-size: 12px;
  color: var(--color-text-muted);
}

@media (max-width: 760px) {
  .footer-main {
    grid-template-columns: 1fr 1fr;
  }
  .brand-col {
    grid-column: 1 / -1;
  }
}

@media (max-width: 480px) {
  .footer-main {
    grid-template-columns: 1fr;
  }
}
</style>
