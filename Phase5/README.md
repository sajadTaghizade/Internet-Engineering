# فاز ۵ — Authentication & Authorization

این فاز روی کد فاز ۴ (`Phase4/Web-Server`) سوار شده است. کل پروژه در `Phase5/Web-Server` کپی و سپس
قابلیت‌های احراز هویت (Authentication) و کسب اجازه (Authorization) طبق `Auth-Description.pdf` به آن
اضافه شده‌اند. معماری هندرول فاز ۴ (سرور HTTP دستی روی Socket، Router ساده، JSON دستی) حفظ شده و
همه‌چیز با همان سبک نوشته شده تا یکدست بماند.

## چه چیزی اضافه/تغییر کرده است

### Backend

**فایل‌های جدید**

| فایل | نقش |
|---|---|
| `model/User.java` | Entity کاربر: `username`، `passwordHash`، `passwordSalt`، `email`، `phone` (با Unique Constraint روی هر سه‌ی username/email/phone) |
| `repository/UserRepository.java` | `findByUsername`, `existsByUsername/Email/Phone` |
| `security/PasswordUtil.java` | Hash کردن رمزعبور با **PBKDF2WithHmacSHA256** + salt تصادفی |
| `security/JwtUtil.java` | تولید و اعتبارسنجی **JWT (HS256)** به‌صورت دستی (بدون کتابخانه) |
| `security/AuthGuard.java` | استخراج `Bearer token` از هدر `Authorization` که در params سرور قرار می‌گیرد |
| `dynamiccontentserver/AuthService.java` | منطق ثبت‌نام/ورود + اعتبارسنجی فرمت |
| `dynamiccontentserver/UserService.java` | ویرایش email/phone و تغییر رمزعبور |
| `dynamiccontentserver/Validators.java` | regex های مشترک برای email/phone/password |
| `dynamiccontentserver/RegisterController.java` | `POST /api/auth/register` |
| `dynamiccontentserver/LoginController.java` | `POST /api/auth/login` |
| `dynamiccontentserver/ProfileController.java` | `GET /api/users/me` (پروفایل + لیست مقالات کاربر) |
| `dynamiccontentserver/UpdateContactController.java` | `POST /api/users/me/contact` |
| `dynamiccontentserver/ChangePasswordController.java` | `POST /api/users/me/password` |

**فایل‌های تغییر‌یافته نسبت به فاز ۴**

- `model/Article.java`: دو فیلد `authorId` و `authorUsername` اضافه شد تا مشخص باشد هر مقاله را
  چه کاربری منتشر کرده و بتوان در پروفایل، مقالات کاربر را لیست کرد.
- `repository/ArticleRepository.java`: متد `findByAuthorId` اضافه شد.
- `dynamiccontentserver/ArticleService.java`: `createArticle` اکنون `authorId`/`authorUsername` را
  می‌گیرد و متد `listArticlesByAuthor` برای صفحه‌ی پروفایل اضافه شده است.
- `dynamiccontentserver/ArticleController.java`: `POST /api/articles` اکنون نیازمند JWT معتبر است؛
  در غیر این صورت `401` برمی‌گرداند. استخراج فیلدهای JSON از منطق تکراری داخل کنترلر به `JsonUtils`
  منتقل شد (حذف کد تکراری، چون همان منطق در کنترلرهای جدید هم لازم بود).
- `dynamiccontentserver/JsonUtils.java`: متدهای عمومی `extractStringField`/`unescapeJsonString`
  (که قبلاً private داخل `ArticleController` بودند)، به‌علاوه‌ی `userToJson`، `authResponseToJson`
  و افزودن `authorId`/`authorUsername` به خروجی `articleToJson`.
- `dynamiccontentserver/HttpResponse.java`: `unauthorized (401)` و `forbidden (403)` اضافه شد.
- `dynamiccontentserver/DynamicContentServer.java`:
  - هدر `Authorization` هنگام پارس کردن Request حالا خوانده و در params (به‌صورت `_authHeader`)
    ذخیره می‌شود (قبلاً فقط `Content-Length` خوانده می‌شد).
  - `Access-Control-Allow-Headers` مقدار `Authorization` را هم شامل می‌شود تا فرانت بتواند هدر
    `Authorization` را در درخواست‌های CORS بفرستد.
  - status line های `401` و `403` اضافه شدند.
  - `main()` سرویس‌ها و `JwtUtil` را از Spring Context گرفته و به `Router` پاس می‌دهد.
  - `@ComponentScan(basePackages = "ir.ac.ut.ece.ie")` اضافه شد: چون کلاس‌های جدید (`security.*`)
    در پکیجی خواهر (نه زیرپکیج) کلاس `@SpringBootApplication` قرار دارند، Component Scan پیش‌فرض
    Spring آن‌ها را نمی‌دید (دقیقاً به همین دلیل فاز ۴ هم برای `model`/`repository` مجبور بود از
    `@EntityScan`/`@EnableJpaRepositories` صریح استفاده کند). این مورد حین تست end-to-end واقعی
    پیدا و رفع شد (پایین را ببینید).
- `dynamiccontentserver/Router.java`: مسیرهای جدید auth/users ثبت شدند.
- `application.properties`: کلید امضای JWT از متغیر محیطی `JWT_SECRET` خوانده می‌شود (با یک مقدار
  پیش‌فرض فقط برای توسعه‌ی محلی) — این برای فاز ۶ (مدیریت secret ها در Docker) هم آماده است.

### Frontend

**فایل‌های جدید**

- `src/context/AuthContext.jsx`: نگهداری وضعیت کاربر لاگین‌شده، `login`/`register`/`logout`/
  `refreshProfile`. توکن در `localStorage` نگه داشته می‌شود و در بارگذاری اولیه‌ی برنامه با یک
  درخواست به `GET /api/users/me` اعتبارسنجی می‌شود.
- `src/components/ProtectedRoute.jsx`: اگر کاربر لاگین نکرده باشد، به `/login` ریدایرکت می‌شود.
- `src/pages/LoginPage.jsx`, `src/pages/RegisterPage.jsx`, `src/pages/ProfilePage.jsx`

**فایل‌های تغییر‌یافته**

- `src/services/api.js`: توابع `register`/`login`/`getProfile`/`updateContact`/`changePassword`،
  ذخیره/خواندن/پاک‌کردن توکن، و افزودن هدر `Authorization: Bearer <token>` به درخواست‌هایی که
  نیاز به احراز هویت دارند (`createArticle` و مسیرهای `/api/users/me*`).
- `src/components/Header.jsx`: بسته به وضعیت لاگین، لینک‌های Login/Register یا Add Article/Profile
  را نشان می‌دهد.
- `src/App.jsx`: `AuthProvider` دور کل اپ، مسیرهای `/login`، `/register`، `/profile` اضافه شدند،
  و `/add-article` و `/profile` داخل `ProtectedRoute` قرار گرفتند.
- `src/pages/ArticlePage.jsx`: نام نویسنده (`authorUsername`) نمایش داده می‌شود.

## سطح دسترسی (Authorization)

- **همه (بدون لاگین)**: `GET /api/articles`, `GET /api/articles/{id}` — یعنی صفحه‌ی اصلی و مشاهده‌ی
  یک مقاله.
- **فقط کاربر لاگین‌کرده**: `POST /api/articles` (افزودن مقاله)، `GET/POST /api/users/me*` (پروفایل،
  ویرایش ایمیل/تلفن، تغییر رمزعبور).
- سمت frontend هم با `ProtectedRoute` همین محدودیت را روی صفحات `/add-article` و `/profile` اعمال
  می‌کند (برای تجربه‌ی کاربری بهتر/ریدایرکت سریع)، اما منبع حقیقت (source of truth) همیشه بک‌اند است:
  حتی اگر کسی مستقیم به API درخواست بزند، بدون توکن معتبر با `401` رد می‌شود.

## چرا این الگوریتم‌ها؟

### Hash رمزعبور: PBKDF2WithHmacSHA256 + Salt تصادفی

- توی JDK خود جاوا (`javax.crypto`) موجود است و نیازی به اضافه‌کردن کتابخانه‌ی جدید (bcrypt/argon2)
  به پروژه نبود.
- برخلاف یک هش ساده مثل SHA-256، به‌عمد کند است و تعداد iteration (اینجا ۱۲۰٬۰۰۰) قابل تنظیم است؛
  یعنی هر چه سخت‌افزار مهاجم قوی‌تر شود، کافی است iteration بالا برود — دقیقاً توصیه‌ی
  NIST SP 800-132 برای ذخیره‌ی رمزعبور.
- هر کاربر یک salt تصادفی و مستقل (۱۶ بایت از `SecureRandom`) دارد، پس دو کاربر با رمزعبور یکسان
  هرگز hash یکسان نخواهند داشت؛ همین موضوع حمله‌ی Rainbow Table را بی‌اثر می‌کند.
- مقایسه‌ی hash ها هم به‌صورت constant-time انجام می‌شود تا نشتی از طریق Timing Attack نداشته باشیم.

### JWT: HS256 پیاده‌سازی‌شده دستی

- چون فقط یک backend واحد است که هم توکن را صادر می‌کند و هم آن را verify می‌کند، یک الگوریتم
  متقارن (HMAC-SHA256) کافی است و منطقی‌تر از یک الگوریتم نامتقارن (مثل RS256) است که برای زمانی
  طراحی شده که verifier از issuer جدا باشد.
- ساختار توکن (`base64url(header).base64url(payload).base64url(signature)`) دستی پیاده‌سازی شده
  (بدون کتابخانه‌ی jjwt/…) تا با سبک بقیه‌ی پروژه (که JSON و HTTP parsing را هم دستی انجام می‌دهد)
  یکدست بماند و همچنین ساختار واقعی JWT کاملاً شفاف و قابل‌فهم باشد.
- Payload شامل `sub` (شناسه‌ی کاربر)، `username`، `iat` و `exp` است. امضا با `Mac`/`HmacSHA256`
  انجام می‌شود و کلید امضا از متغیر محیطی `JWT_SECRET` خوانده می‌شود (نه هاردکد در کد).
- انقضای پیش‌فرض توکن ۲۴ ساعت است (`app.jwt.expiration-minutes`)، تا هم امنیت معقول باشد و هم
  کاربر مجبور به لاگین مکرر نشود.

## اعتبارسنجی ثبت‌نام (طبق سند فاز ۵)

- `username`, `email`, `phone` باید یکتا باشند.
- حداقل یکی از `email`/`phone` باید داده شود.
- فرمت `email` و `phone` با regex بررسی می‌شود.
- رمزعبور حداقل ۶ کاراکتر و شامل حرف کوچک + حرف بزرگ + عدد.

## نقشه‌ی APIهای جدید

| Method | Path | نیازمند Auth | توضیح |
|---|---|---|---|
| POST | `/api/auth/register` | خیر | ثبت‌نام؛ در پاسخ موفق `token` + `user` برمی‌گرداند |
| POST | `/api/auth/login` | خیر | ورود؛ در پاسخ موفق `token` + `user` |
| GET | `/api/users/me` | بله | پروفایل کاربر + لیست مقالات منتشرشده توسط او |
| POST | `/api/users/me/contact` | بله | ویرایش email/phone |
| POST | `/api/users/me/password` | بله | تغییر رمزعبور (نیازمند رمز فعلی) |
| POST | `/api/articles` | بله | افزودن مقاله (اکنون به کاربر لاگین‌شده متصل می‌شود) |
| GET | `/api/articles`, `/api/articles/{id}` | خیر | بدون تغییر نسبت به فاز ۴ |

## اجرا (بدون Docker — همان روش فاز ۴)

```bash
# پایگاه‌داده (همان docker-compose فاز ۴)
cd Phase5/Web-Server
docker compose up -d

# بک‌اند
export JWT_SECRET="یک-رشته-طولانی-و-تصادفی"
mvn spring-boot:run

# فرانت‌اند (در ترمینال دیگر)
cd src/frontend
npm install
npm run dev
```

## محدودیت‌های شناخته‌شده / نکات

- توکن در `localStorage` نگه‌داری می‌شود، نه در یک httpOnly cookie. این ساده‌ترین گزینه با توجه به
  اینکه سرور دستی فعلی امکانات کوکی/CSRF token پیچیده ندارد، اما در برابر XSS ذاتاً کمی آسیب‌پذیرتر
  از httpOnly cookie است — این تبادل آگاهانه انجام شده است.
- این پروژه به‌صورت واقعی تست شده است: بک‌اند با `mvn package` به یک jar اجراشدنی تبدیل و روی یک
  Postgres 16 واقعی (محلی، نه Docker) اجرا شد و کل جریان با `curl` بررسی شد — ثبت‌نام، رد ثبت‌نام
  تکراری (۴۰۹)، رد رمزعبور ضعیف و رد نبود email/phone (۴۰۰)، ورود، رد ایجاد مقاله بدون توکن (۴۰۱)
  و موفقیت آن با توکن، `GET /api/users/me` (پروفایل + مقالات کاربر)، ویرایش ایمیل/تلفن، تغییر
  رمزعبور (رد رمز فعلی اشتباه، تایید رمز جدید، رد لاگین با رمز قدیمی)، و در‌دسترس‌بودن
  `GET /api/articles` بدون نیاز به لاگین. همه‌ی این موارد طبق انتظار جواب دادند. در همین تست بود که
  باگ Component Scan (بالا) کشف و رفع شد. فرانت‌اند هم با `npm run build` بدون خطا build شد.
- تنها چیزی که در sandbox این نشست قابل تست نبود، خودِ Docker است (نبود دسترسی به Docker daemon)؛
  جزئیات در `Phase6/README.md` آمده است.
