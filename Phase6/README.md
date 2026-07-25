# فاز ۶ — Docker

این فاز روی کد فاز ۵ (`Phase5/Web-Server`، شامل قابلیت‌های Auth) سوار شده است. کل پروژه به
`Phase6/Web-Server` کپی شده و طبق `Docker-Description.pdf`، بک‌اند و فرانت‌اند Dockerize شده و یک
`docker-compose.yml` اضافه شده که پایگاه‌داده + بک‌اند + فرانت‌اند را با هم بالا می‌آورد. **کد
اپلیکیشن (منطق auth/article) نسبت به فاز ۵ تغییری نکرده**؛ تنها دو تغییر کوچک لازم بود تا داخل
Docker درست کار کند (پایین توضیح داده شده) — بقیه‌ی این فاز صرفاً زیرساخت Docker است.

> **به‌روزرسانی:** بعداً در فاز ۵ دو اصلاح روی کد اعمال شد — رابطه‌ی `Article`↔`User` به یک
> `@ManyToOne` واقعی (با Foreign Key در دیتابیس) تبدیل شد و یک لایه‌ی `UserDto` برای جلوگیری از
> عبور مستقیم entity کاربر (شامل `passwordHash`/`passwordSalt`) بین لایه‌ها اضافه شد. همان دو
> اصلاح روی سورس‌های جاوای `Phase6/Web-Server` هم اعمال شد (سورس‌های بک‌اند این فاز اکنون کاملاً با
> `Phase5/Web-Server` یکسان‌اند)؛ جزئیات در بخش «همگام‌سازی با اصلاحات فاز ۵» پایین آمده.

## فایل‌های جدید

| فایل | نقش |
|---|---|
| `Dockerfile` | Dockerize بک‌اند (جاوا/Spring) با Multi-stage build |
| `.dockerignore` | جلوگیری از فرستادن `target/`, `src/frontend/`, و غیره به build context بک‌اند |
| `src/frontend/Dockerfile` | Dockerize فرانت‌اند (React) با Multi-stage build |
| `src/frontend/nginx.conf` | تنظیمات Nginx: سرو فایل‌های استاتیک + Reverse Proxy به بک‌اند |
| `src/frontend/.dockerignore` | جلوگیری از فرستادن `node_modules/`, `dist/` به build context فرانت‌اند |
| `.env.example` | نمونه‌ی متغیرهای محیطی/secret های موردنیاز (بدون مقدار واقعی) |

## فایل‌های تغییر‌یافته نسبت به فاز ۵

- `docker-compose.yml`: قبلاً فقط سرویس Postgres را تعریف می‌کرد؛ حالا سه سرویس
  (`postgres`, `backend`, `frontend`) روی یک network مشترک تعریف شده‌اند.
- `pom.xml`: پلاگین `spring-boot-maven-plugin` (goal `repackage`) اضافه شد. **چرا لازم بود؟** پروژه
  تا فاز ۵ فقط با `mvn spring-boot:run` اجرا می‌شد و `mvn package` یک jar «نازک» (بدون
  dependency ها و بدون Main-Class اجراشدنی) می‌ساخت. برای اجرا در یک container با `java -jar`،
  به یک jar کاملاً مستقل و اجراشدنی (fat/executable jar) نیاز است که این پلاگین می‌سازد.
- `src/main/resources/application.properties`: آدرس/پورت/نام/کاربر/رمز پایگاه‌داده دیگر هاردکد
  نیستند و از متغیرهای محیطی `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD` خوانده می‌شوند
  (با مقدار پیش‌فرض برای اجرای محلی بدون Docker). این تغییر لازم بود چون داخل `docker-compose`،
  بک‌اند باید به‌جای `localhost`، به هاست `postgres` (نام سرویس) وصل شود.
- `src/main/java/.../DynamicContentServer.java`: یک `@ComponentScan(basePackages = "ir.ac.ut.ece.ie")`
  اضافه شد. این یک باگ از فاز ۵ بود (نه چیزی که فاز ۶ اضافه کرده باشد) که حین تست واقعی end-to-end
  بک‌اند (اجرای jar روی یک Postgres واقعی، قبل از رفتن سراغ Docker) کشف شد: کلاس‌های پکیج
  `ir.ac.ut.ece.ie.security` (مثل `JwtUtil`) چون در زیرپکیجِ کلاس `@SpringBootApplication` نبودند،
  توسط Component Scan پیش‌فرض اسپرینگ دیده نمی‌شدند و اپلیکیشن اصلاً بالا نمی‌آمد. جزئیات کامل در
  `Phase5/README.md` هم آمده و همان‌جا هم رفع شده (چون به فاز ۵ مربوط است)، این‌جا صرفاً برای
  کامل‌بودن تاریخچه‌ی تغییرات ذکر شد.
- `.gitignore` (در ریشه‌ی ریپو): خط `.env` اضافه شد تا secret های واقعی هرگز commit نشوند.

هیچ فایل دیگری (منطق Article/Auth/User، صفحات React، و غیره) نسبت به فاز ۵ تغییر نکرده است.

## همگام‌سازی با اصلاحات فاز ۵ (DTO و رابطه‌ی مقاله↔کاربر)

`Phase6/Web-Server` وقتی ساخته شد که هنوز `Phase5/Web-Server` دو ایراد داشت: رابطه‌ی `Article`↔`User`
با دو ستون خام `authorId`/`authorUsername` (بدون Foreign Key واقعی) پیاده شده بود، و entity خام
`User` (شامل `passwordHash`/`passwordSalt`) بین Service و Controller رد و بدل می‌شد. بعد از رفع این
دو مورد در فاز ۵، همان فایل‌ها عیناً در `Phase6/Web-Server` هم جایگزین شدند تا کد بک‌اند این فاز با
فاز ۵ کاملاً همگام بماند (تأیید شده با `diff -rq` بین `src/main/java` دو فاز):

- `model/Article.java`, `repository/ArticleRepository.java`,
  `dynamiccontentserver/ArticleService.java`, `dynamiccontentserver/ArticleController.java`:
  رابطه‌ی `@ManyToOne` واقعی به `User` به‌جای ستون‌های خام.
- `dto/UserDto.java` (فایل جدید)، `dynamiccontentserver/AuthService.java`,
  `dynamiccontentserver/UserService.java`, `dynamiccontentserver/JsonUtils.java`,
  `dynamiccontentserver/ProfileController.java`: عبور `UserDto` به‌جای entity خام `User` بین Service
  و Controller.

فایل‌های مخصوص فاز ۶ (`pom.xml`, `application.properties` با env varهای `DB_*`, `Dockerfile`,
`docker-compose.yml`, `.env.example`) به این تغییرات نیازی نداشتند و دست‌نخورده ماندند، چون فقط به
منطق دامنه مربوط بودند نه به نحوه‌ی اجرا/deploy.

## Backend Dockerization (`Dockerfile`)

Multi-stage build با دو stage:

1. **Build stage** — `maven:3.9-eclipse-temurin-21`: پروژه‌ی جاوا را با Maven کامپایل و به یک
   executable jar (`dynamiccontentserver-*.jar`) پکیج می‌کند. `pom.xml` قبل از `src/` کپی و
   `dependency:go-offline` اجرا می‌شود تا لایه‌ی Docker مربوط به دانلود dependency ها جدا از کد
   بماند و با تغییر کد دوباره دانلود نشود.
2. **Runtime stage** — `eclipse-temurin:21-jre-alpine`: فقط JRE (نه JDK کامل، نه Maven) دارد.
   تنها jar ساخته‌شده از stage اول کپی می‌شود و با `java -jar app.jar` اجرا می‌شود.

**چرا این base image ها؟**
- Stage اول به یک JDK کامل + Maven نیاز دارد که فقط برای *ساختن* jar لازم است.
- Stage دوم فقط باید jar را *اجرا* کند، پس یک JRE کافی است؛ نیازی به Maven/JDK/کد منبع در image
  نهایی نیست. Alpine هم نسبت به image های مبتنی بر Debian/Ubuntu بسیار سبک‌تر است (حدود چند ده
  مگابایت به‌جای چند صد مگابایت)، و چون این سرویس backend هیچ ابزار سیستم‌عامل خاصی نیاز ندارد،
  Alpine انتخاب مناسبی است.
- کانتینر با یک کاربر غیر-root (`app`) اجرا می‌شود (نه root) که یک اصل امنیتی پایه است.

## Frontend Dockerization (`src/frontend/Dockerfile`)

Multi-stage build:

1. **Build stage** — `node:22-alpine`: `npm ci` و سپس `npm run build` که خروجی build شده و
   بهینه‌ی React (فایل‌های استاتیک HTML/CSS/JS) را در `dist/` تولید می‌کند.
2. **Runtime stage** — `nginx:1.27-alpine`: فقط فایل‌های `dist/` را از stage اول کپی می‌کند و
   Nginx به‌عنوان یک static file server آن‌ها را serve می‌کند.

**چرا این base image ها؟** ابزارهای توسعه (Vite dev server، `npm start`) برای Production مناسب
نیستند (طبق سند فاز ۶). Node فقط برای *build* لازم است؛ در Production فقط باید فایل‌های استاتیکِ
از قبل build شده سرو شوند که یک وب‌سرور سبک مثل Nginx برای این کار ایده‌آل است — سریع‌تر، سبک‌تر، و
امن‌تر از نگه‌داشتن کل toolchain نود در image نهایی.

## Reverse Proxy (`src/frontend/nginx.conf`)

Nginx در کانتینر فرانت‌اند به‌عنوان reverse proxy بین کاربر و بک‌اند عمل می‌کند:

```
location /api/ {
    proxy_pass http://backend:9092/api/;
    ...
}

location / {
    try_files $uri /index.html;
}
```

- هر درخواستی به `/api/...` به کانتینر `backend` روی پورت داخلی `9092` فوروارد می‌شود. چون هر دو
  کانتینر روی یک Docker network هستند، نام سرویس `backend` مستقیماً به IP آن کانتینر resolve
  می‌شود — نیازی به دانستن IP واقعی نیست.
- بقیه‌ی مسیرها (`/`, `/login`, `/article/5`, ...) به `index.html` برمی‌گردند تا React Router
  (که client-side routing دارد) خودش مسیر را مدیریت کند.
- **مزیت این روش:** از دید مرورگر، فرانت‌اند و بک‌اند روی یک origin هستند (بدون نیاز به CORS واقعی
  در Production، و بدون نیاز به هاردکد کردن آدرس بک‌اند در کد فرانت‌اند — دقیقاً همان چیزی که در
  توسعه‌ی محلی با proxy داخل `vite.config.js` هم انجام می‌شد).
- **هزینه/عیب:** یک لایه‌ی اضافه بین کاربر و بک‌اند (کمی latency و یک نقطه‌ی پیکربندی بیشتر)، و
  Nginx باید به‌روز و امن نگه داشته شود؛ در مقابلش، امکاناتی مثل TLS termination یکجا، محدود کردن
  مسیرهای در‌دسترس، و مخفی‌کردن توپولوژی داخلی سرویس‌ها از دنیای بیرون به‌دست می‌آید.

## Database Dockerization

طبق سند، برای پایگاه‌داده نیازی به `Dockerfile` جدید نبود: از ایمیج رسمی `postgres:16-alpine`
استفاده شده و از طریق Environment Variables (`POSTGRES_DB`/`POSTGRES_USER`/`POSTGRES_PASSWORD`)
پیکربندی می‌شود.

## Docker Compose (`docker-compose.yml`)

سه سرویس:

- **`postgres`**: ایمیج رسمی، با یک **named volume** (`postgres_data`) که روی
  `/var/lib/postgresql/data` mount می‌شود — چون کانتینرها به‌طور پیش‌فرض موقتی (Ephemeral) هستند،
  بدون این volume با هر بار `docker compose down`/`up` تمام کاربران و مقالات از بین می‌رفتند. پورت
  ۵۴۳۲ به هاست publish **نشده** (فقط داخل شبکه‌ی داخلی Docker در‌دسترس است)، چون فقط بک‌اند باید به
  آن وصل شود.
- **`backend`**: از `Dockerfile` بک‌اند build می‌شود. با `depends_on` + `healthcheck` (بر پایه‌ی
  `pg_isready`) تضمین می‌شود که بک‌اند فقط بعد از آماده‌شدن واقعیِ Postgres تلاش به اتصال کند (نه
  فقط بعد از start شدن کانتینر). آدرس پایگاه‌داده از طریق `DB_HOST=postgres` (نام سرویس) تنظیم
  می‌شود.
- **`frontend`**: از `src/frontend/Dockerfile` build می‌شود، بعد از `backend` بالا می‌آید، و پورت
  ۸۰ آن (قابل تغییر با `FRONTEND_PORT`) به هاست publish می‌شود — تنها نقطه‌ی ورودی که از بیرون
  در‌دسترس است.

همه‌ی سه سرویس روی یک network مشترک (`articleshare-net`) هستند تا با نام سرویس همدیگر را resolve
کنند (مثلاً `http://backend:9092` یا `postgres:5432`).

### مدیریت Secret ها

طبق سند، حفاظت از مقادیر حساس مهم است. ساده‌ترین و رایج‌ترین روش برای Docker Compose انتخاب شد:

- تمام مقادیر حساس (`POSTGRES_PASSWORD`, `JWT_SECRET`) در `docker-compose.yml` هاردکد **نیستند**؛
  با `${VAR:?...}` از یک فایل `.env` خوانده می‌شوند و اگر تنظیم نشده باشند، `docker compose` با یک
  پیغام خطای واضح متوقف می‌شود (به‌جای اجرا با یک مقدار پیش‌فرض ناامن).
- `.env.example` (بدون مقدار واقعی) commit شده تا مشخص باشد چه متغیرهایی لازم است؛ خودِ `.env`
  در `.gitignore` ریشه‌ی ریپو قرار گرفته و هرگز commit نمی‌شود.
- در Production واقعی، گام بعدی معمولاً استفاده از `docker secrets` یا یک secret manager (Vault،
  AWS Secrets Manager، ...) است؛ اما طبق نکته‌ی پایانیِ سند («به ساده‌ترین شکل ممکن امنیت آن‌ها را
  تامین کنید»)، فایل `.env` خارج از git برای این پروژه‌ی درسی کافی و متناسب است.

## نحوه‌ی اجرا

```bash
cd Phase6/Web-Server
cp .env.example .env
# .env را ویرایش کنید و مقدار واقعی POSTGRES_PASSWORD و JWT_SECRET را بگذارید

docker compose up -d --build
# فرانت‌اند: http://localhost:80
# بک‌اند از طریق فرانت‌اند/Nginx در دسترس است: http://localhost/api/...
```

برای توقف: `docker compose down` (داده‌های پایگاه‌داده در volume باقی می‌ماند) یا
`docker compose down -v` برای پاک‌کردن کامل داده‌ها هم.

## محدودیت‌های تست در این نشست

در نشستی که فایل‌های فاز ۶ اولیه نوشته شدند، اصلاً به Docker daemon دسترسی نبود. در این نشست (که
اصلاحات فاز ۵ را همگام کرد)، یک Docker daemon واقعی در دسترس بود؛ آنچه واقعاً تست/تایید شد:

- **کامپایل و پکیج بک‌اند**: بعد از همگام‌سازی فایل‌های جاوا، `mvn compile` و `mvn package
  -DskipTests` هر دو بدون خطا روی `Phase6/Web-Server` اجرا شدند و یک jar اجراشدنی واقعی ساختند
  (`target/dynamiccontentserver-1.0.0-SNAPSHOT.jar`, ~۴۲ مگابایت).
- **تست end-to-end واقعی روی Postgres واقعی (بدون Docker)**: همان jar با `java -jar` (دقیقاً همان
  دستوری که Runtime stage در `Dockerfile` هم اجرا می‌کند) روی یک Postgres 16 نصب‌شده محلی اجرا شد.
  با بررسی مستقیم اسکیمای ساخته‌شده (`\d articles` در psql) تأیید شد که یک **Foreign Key واقعی**
  (`articles.author_id → users.id`) ساخته شده و ستون تکراری `author_username` دیگر وجود ندارد.
  سپس کل جریان با `curl` تست شد: ثبت‌نام، رد ثبت‌نام تکراری (۴۰۹)، رد رمزعبور ضعیف (۴۰۰)، ورود، رد
  ایجاد مقاله بدون توکن (۴۰۱) و موفقیت آن با توکن (که `authorId`/`authorUsername` را درست از روی
  رابطه‌ی جدید در پاسخ نشان داد)، `GET /api/articles` و `GET /api/articles/{id}` بدون نیاز به لاگین،
  `GET /api/users/me` (پروفایل + مقالات کاربر، از طریق `UserDto`)، ویرایش ایمیل/تلفن، و تغییر
  رمزعبور (رد رمز فعلی اشتباه، تأیید رمز جدید، رد لاگین با رمز قدیمی، موفقیت لاگین با رمز جدید).
  همه‌ی این موارد طبق انتظار جواب دادند.
- **فرانت‌اند**: `npm install` و `npm run build` روی `src/frontend` بدون خطا اجرا شدند.
- **اعتبارسنجی `docker-compose.yml`**: `docker compose config` هم حالت خطا (وقتی `.env` نیست، پیغام
  واضح `Set POSTGRES_PASSWORD in .env, see .env.example` را نشان می‌دهد) و هم حالت موفق (با یک
  `.env` تستی، تمام متغیرهای `${...}` درست resolve شدند) را تأیید کرد.
- **اجرای واقعی `docker compose up --build`**: با Docker daemon واقعی امتحان شد، اما در همان قدم
  اول (`Image postgres:16-alpine Pulling`) با خطای `403 Forbidden` از
  `production.cloudfront.docker.com` متوقف شد. این خطا مربوط به کد یا تنظیمات این پروژه نیست؛ خروجی
  endpoint وضعیت پراکسیِ همین محیط sandbox (`/__agentproxy/status`) این را صراحتاً به‌عنوان یک
  **policy denial** در سطح شبکه‌ی سازمانی گزارش می‌دهد (`connect_rejected`، `gateway answered 403 to
  CONNECT`، host: `production.cloudfront.docker.com:443`) — یعنی pull از Docker Hub برای این sandbox
  عمداً مسدود است، نه اینکه `Dockerfile`/`docker-compose.yml` مشکلی داشته باشند.
- **نتیجه**: تمام بخش‌های pipeline که به pull کردن image از یک رجیستری بیرونی وابسته نبودند
  (کامپایل، پکیج، اجرای واقعی jar روی Postgres واقعی، build فرانت‌اند، پارس/resolve شدن
  docker-compose) عملاً اجرا و تأیید شدند. تنها گام تأییدنشده، خودِ `docker build`/`docker compose
  up` به‌خاطر بلاک‌شدن Docker Hub در سطح شبکه‌ی این sandbox است — این محدودیت محیط اجراست، نه یک
  نقص در فایل‌های فاز ۶.
