Solace - Nền tảng Hỗ trợ Sức khỏe Tâm lý 🌟
English | Tiếng Việt

Tiếng Việt
Giới thiệu 🌈
Solace là nền tảng web tạo không gian an toàn và minh bạch cho Gen Z và Millennials chia sẻ cảm xúc, kết nối cộng đồng, và chăm sóc sức khỏe tâm lý. Với thiết kế tập trung vào trải nghiệm người dùng, khả năng tiếp cận, và bảo mật, Solace khuyến khích biểu đạt cảm xúc công khai nhưng vẫn đảm bảo trách nhiệm qua xác minh danh tính. Công nghệ hiện đại và AI (Gemini) được tích hợp để tối ưu phát triển và tương tác.
Khẩu hiệu: Chia sẻ trái tim, tìm thấy bình yên 💖
Tính năng nổi bật ✨

📝 Chia sẻ bài viết: Tạo bài viết (văn bản, hình ảnh, video) phân loại tích cực/tiêu cực.
😊 Phản hồi cảm xúc: Tương tác qua các cảm xúc như “đồng cảm”, “truyền cảm hứng”, “ủng hộ”.
💬 Nhắn tin riêng: Kết nối sâu hơn qua hệ thống tin nhắn cá nhân.
🚨 Báo cáo vi phạm: Báo cáo nội dung không phù hợp để duy trì môi trường an toàn.
🛠️ Bảng quản trị Admin: Quản lý người dùng, bài viết, báo cáo, và từ cấm.
🔔 Thông báo thời gian thực: Cập nhật tức thì cho bình luận, phản hồi, báo cáo.
📚 Blog tối ưu SEO: Nội dung giáo dục về sức khỏe tâm lý, thu hút truy cập.
♿ Khả năng tiếp cận: Tuân thủ WCAG 2.1 (hỗ trợ trình đọc màn hình, độ tương phản cao).

Công nghệ sử dụng 🛠️
Frontend

React.js: Giao diện động, tái sử dụng. 🚀
Next.js: Server-Side Rendering cho hiệu suất và SEO. 🌐
Tailwind CSS: Thiết kế responsive, đồng nhất. 🎨
Cloudinary: Quản lý và tối ưu hóa media. 🖼️

Backend

Node.js & Express: API RESTful xử lý yêu cầu nhanh. ⚙️
PostgreSQL: Cơ sở dữ liệu quan hệ mạnh mẽ. 🗄️
JWT & OAuth 2.0: Xác thực an toàn, đăng nhập qua Google. 🔐

DevOps

Docker: Môi trường phát triển/triển khai nhất quán. 🐳
GitHub Actions: Tự động hóa CI/CD. 🤖
Vercel: Triển khai frontend dễ dàng. 🌍

AI

Gemini: Hỗ trợ viết code, tối ưu SEO, gợi ý UI, phân tích dữ liệu. 🤖

Kiến trúc hệ thống 🏗️
Solace sử dụng Modular Monolith với các module độc lập (Users, Posts, Emotions, Reports, Messages, Admin) để dễ bảo trì và mở rộng:

Frontend: React/Next.js gọi API backend.
Backend: Node.js/Express xử lý logic và tương tác cơ sở dữ liệu.
Cơ sở dữ liệu: PostgreSQL với schema phân tách.
Lưu trữ media: Cloudinary tối ưu hình ảnh/video.

Hướng dẫn cài đặt ⚙️
Yêu cầu

Node.js (v18.x+)  
PostgreSQL (v15.x+)  
Docker (tùy chọn)  
Tài khoản Cloudinary  
Git

Các bước cài đặt

Sao chép repository:
git clone https://github.com/your-username/solace.git
cd solace


Cài đặt phụ thuộc:
# Backend
cd backend
npm install

# Frontend
cd ../frontend
npm install


Cấu hình biến môi trường:Tạo file .env trong thư mục backend và frontend dựa trên .env.example:
# Backend .env
DATABASE_URL=postgresql://user:password@localhost:5432/solace
JWT_SECRET=your_jwt_secret
CLOUDINARY_URL=your_cloudinary_url

# Frontend .env
NEXT_PUBLIC_API_URL=http://localhost:3001
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your_cloud_name


Thiết lập cơ sở dữ liệu:
cd backend
npm run migrate


Khởi chạy ứng dụng:
# Backend
cd backend
npm start

# Frontend
cd ../frontend
npm run dev


Truy cập:Mở http://localhost:3000 trên trình duyệt. 🌐


Docker (Tùy chọn)
docker-compose up --build

Truy cập tại http://localhost:3000.
Sử dụng 📖

Người dùng: Đăng ký/đăng nhập để chia sẻ bài viết, phản hồi, nhắn tin, hoặc báo cáo.
Quản trị viên: Truy cập /admin để quản lý nội dung và người dùng.
Blog: Xem nội dung về sức khỏe tâm lý và câu chuyện cộng đồng.

Kiểm thử 🧪

Unit Tests: npm test (Jest, React Testing Library).
Integration Tests: npm run test:integration (Supertest, backend).
E2E Tests: npm run cypress:open (Cypress, frontend).
Hiệu suất: Sử dụng Google PageSpeed Insights.

Bảo mật 🔒

Xác thực: JWT và OAuth 2.0.
Mã hóa: Mật khẩu băm (bcrypt), dữ liệu nhạy cảm (AES-256).
Kiểm tra đầu vào: Ngăn SQL injection, XSS.
Giới hạn tốc độ: Bảo vệ API khỏi lạm dụng.

SEO & Truyền thông 📣

Từ khóa: “Chia sẻ cảm xúc”, “Hỗ trợ tâm lý”, “Cộng đồng sức khỏe tinh thần”.
Chiến lược: Blog, TikTok, Instagram nhắm đến Gen Z và Millennials.
Phân tích: Google Analytics theo dõi hành vi.

Phát triển tương lai 🚀

🤖 Chatbot AI: Hỗ trợ tâm lý thời gian thực với Gemini.
👥 Nhóm riêng tư: Thảo luận theo chủ đề.
📱 Ứng dụng di động: iOS/Android.
🏅 Thưởng: Huy hiệu cho đóng góp tích cực.

Đóng góp 🤝

Fork repository.
Tạo nhánh: git checkout -b feature/your-feature.
Commit: git commit -m "Thêm tính năng".
Push: git push origin feature/your-feature.
Mở Pull Request.

Giấy phép 📜
Cấp phép theo MIT License. Xem LICENSE.
Liên hệ 📧
Liên hệ: solace.support@example.com.

English
Introduction 🌟
Solace is a web platform offering a safe and transparent space for Gen Z and Millennials to share emotions, connect with a supportive community, and promote mental well-being. Designed with user experience, accessibility, and security in mind, Solace fosters open emotional expression while ensuring accountability via verified identities. Modern tech and AI tools (Gemini) enhance development and engagement.
Slogan: Share Your Heart, Find Your Peace 💖
Key Features ✨

📝 Post Sharing: Create and share text, image, or video posts, categorized as positive/negative.
😊 Emotion Reactions: Engage with posts via “empathy,” “inspiration,” or “support.”
💬 Direct Messaging: Private chats for deeper connections.
🚨 Report System: Flag inappropriate content for a safe environment.
🛠️ Admin Dashboard: Manage users, posts, reports, and banned keywords.
🔔 Real-Time Notifications: Updates for comments, reactions, or reports.
📚 SEO-Optimized Blog: Mental health content to drive traffic.
♿ Accessibility: WCAG 2.1 compliant (screen reader support, high contrast).

Tech Stack 🛠️
Frontend

React.js: Dynamic, reusable UI components. 🚀
Next.js: SSR for performance and SEO. 🌐
Tailwind CSS: Responsive, consistent styling. 🎨
Cloudinary: Media storage and optimization. 🖼️

Backend

Node.js & Express: RESTful API for efficient requests. ⚙️
PostgreSQL: Robust relational database. 🗄️
JWT & OAuth 2.0: Secure authentication, third-party login (Google). 🔐

DevOps

Docker: Consistent environments. 🐳
GitHub Actions: Automated CI/CD. 🤖
Vercel: Easy frontend deployment. 🌍

AI

Gemini: Code generation, SEO content, UI suggestions, data analysis. 🤖

System Architecture 🏗️
Solace uses a Modular Monolith architecture with independent modules (Users, Posts, Emotions, Reports, Messages, Admin) for maintainability and scalability:

Frontend: React/Next.js, API-driven UI.
Backend: Node.js/Express for logic and database interaction.
Database: PostgreSQL with modular schema.
Media Storage: Cloudinary for optimized images/videos.

Installation ⚙️
Requirements

Node.js (v18.x+)  
PostgreSQL (v15.x+)  
Docker (optional)  
Cloudinary account  
Git

Setup Steps

Clone Repository:
git clone https://github.com/your-username/solace.git
cd solace


Install Dependencies:
# Backend
cd backend
npm install

# Frontend
cd ../frontend
npm install


Environment Variables:Create .env files in backend and frontend based on .env.example:
# Backend .env
DATABASE_URL=postgresql://user:password@localhost:5432/solace
JWT_SECRET=your_jwt_secret
CLOUDINARY_URL=your_cloudinary_url

# Frontend .env
NEXT_PUBLIC_API_URL=http://localhost:3001
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your_cloud_name


Database Setup:
cd backend
npm run migrate


Run Application:
# Backend
cd backend
npm start

# Frontend
cd ../frontend
npm run dev


Access:Open http://localhost:3000. 🌐


Docker (Optional)
docker-compose up --build

Access at http://localhost:3000.
Usage 📖

Users: Register/login to post, react, message, or report.
Admins: Access /admin to manage content/users.
Blog: Explore mental health resources and stories.

Testing 🧪

Unit Tests: npm test (Jest, React Testing Library).
Integration Tests: npm run test:integration (Supertest, backend).
E2E Tests: npm run cypress:open (Cypress, frontend).
Performance: Use Google PageSpeed Insights.

Security 🔒

Authentication: JWT, OAuth 2.0.
Encryption: Bcrypt for passwords, AES-256 for sensitive data.
Input Validation: Prevents SQL injection, XSS.
Rate Limiting: Protects APIs from abuse.

SEO & Marketing 📣

Keywords: “Share emotions,” “mental health support,” “online therapy.”
Strategy: Blog, TikTok, Instagram for Gen Z/Millennials.
Analytics: Google Analytics for user behavior.

Future Development 🚀

🤖 AI Chatbot: Real-time mental health support with Gemini.
👥 Private Groups: Topic-based discussions.
📱 Mobile App: iOS/Android apps.
🏅 Rewards: Badges for positive contributions.

Contributing 🤝

Fork repository.
Create branch: git checkout -b feature/your-feature.
Commit: git commit -m "Add feature".
Push: git push origin feature/your-feature.
Open Pull Request.

License 📜
MIT License. See LICENSE.
Contact 📧
Reach out: solace.support@example.com.
