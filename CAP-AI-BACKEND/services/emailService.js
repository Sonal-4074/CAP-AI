const nodemailer = require('nodemailer');

class EmailService {
  constructor() {
    this.transporter = null;
    this.isConfigured = false;
    this.init();
  }

  init() {
    const user = process.env.EMAIL_USER;
    const pass = process.env.EMAIL_PASS;

    if (user && pass && user !== 'your_email@gmail.com') {
      try {
        this.transporter = nodemailer.createTransport({
          service: 'gmail',
          auth: { user, pass }
        });
        this.isConfigured = true;
        console.log(`📧 [Email Service]: Transporter initialized with user ${user}`);
      } catch (err) {
        console.error('❌ [Email Service Init Error]:', err.message);
        this.isConfigured = false;
      }
    } else {
      this.isConfigured = false;
      console.warn('⚠️ [Email Service]: EMAIL_USER / EMAIL_PASS not configured. OTPs will be displayed in server logs for local verification.');
    }
  }

  /**
   * Send Email Verification OTP
   */
  async sendVerificationOtp(email, otp, fullName = 'Aspirant') {
    console.log(`\n======================================================`);
    console.log(`📩 [EMAIL OTP SENT]: Target -> ${email}`);
    console.log(`🔑 Verification OTP Code: ${otp}`);
    console.log(`======================================================\n`);

    if (!this.isConfigured || !this.transporter) {
      return { sent: false, reason: 'unconfigured_local_fallback', debugOtp: otp };
    }

    try {
      const mailOptions = {
        from: process.env.EMAIL_FROM || `CAP AI Support <${process.env.EMAIL_USER}>`,
        to: email,
        subject: `Your CAP AI Verification Code: ${otp}`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 550px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
            <div style="text-align: center; margin-bottom: 20px;">
              <h2 style="color: #101B3D; margin: 0;">CAP AI</h2>
              <p style="color: #64748b; font-size: 14px; margin-top: 4px;">Current Affairs Preparation with AI</p>
            </div>
            <p style="color: #334155; font-size: 15px;">Hello <strong>${fullName}</strong>,</p>
            <p style="color: #334155; font-size: 15px; line-height: 1.5;">Thank you for registering on CAP AI. Use the 6-digit one-time password below to verify your email address:</p>
            <div style="text-align: center; margin: 30px 0;">
              <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #355CDE; background-color: #f1f5f9; padding: 12px 24px; border-radius: 8px; border: 1px dashed #cbd5e1; display: inline-block;">
                ${otp}
              </span>
            </div>
            <p style="color: #64748b; font-size: 13px;">This code is valid for 15 minutes. For security, never share this code with anyone.</p>
            <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 25px 0;" />
            <p style="color: #94a3b8; font-size: 11px; text-align: center;">CAP AI Education Platform · Empowering India's Exam Aspirants</p>
          </div>
        `
      };

      const info = await this.transporter.sendMail(mailOptions);
      console.log(`✅ Verification email delivered: ${info.messageId}`);
      return { sent: true, messageId: info.messageId };
    } catch (error) {
      console.error(`❌ Failed to send verification email to ${email}:`, error.message);
      return { sent: false, reason: error.message, debugOtp: otp };
    }
  }

  /**
   * Send Password Reset OTP
   */
  async sendPasswordResetOtp(email, otp) {
    console.log(`\n======================================================`);
    console.log(`📩 [PASSWORD RESET OTP]: Target -> ${email}`);
    console.log(`🔑 Reset Code: ${otp}`);
    console.log(`======================================================\n`);

    if (!this.isConfigured || !this.transporter) {
      return { sent: false, reason: 'unconfigured_local_fallback', debugOtp: otp };
    }

    try {
      const mailOptions = {
        from: process.env.EMAIL_FROM || `CAP AI Support <${process.env.EMAIL_USER}>`,
        to: email,
        subject: `CAP AI Password Reset Code: ${otp}`,
        html: `
          <div style="font-family: Arial, sans-serif; max-width: 550px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 12px;">
            <h2 style="color: #101B3D;">Reset Your Password</h2>
            <p style="color: #334155; font-size: 15px;">We received a request to reset your password. Use the code below:</p>
            <div style="text-align: center; margin: 25px 0;">
              <span style="font-size: 28px; font-weight: bold; letter-spacing: 6px; color: #DC2626; background: #FEF2F2; padding: 10px 20px; border-radius: 6px;">
                ${otp}
              </span>
            </div>
            <p style="color: #64748b; font-size: 13px;">If you did not request this, please ignore this email.</p>
          </div>
        `
      };
      const info = await this.transporter.sendMail(mailOptions);
      return { sent: true, messageId: info.messageId };
    } catch (error) {
      console.error(`❌ Failed to send password reset email:`, error.message);
      return { sent: false, reason: error.message, debugOtp: otp };
    }
  }
}

module.exports = new EmailService();
