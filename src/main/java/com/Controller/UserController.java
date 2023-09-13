package com.Controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Entity.User;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@RestController
@RequestMapping("/user")
public class UserController {

	private static final String PDF_STORAGE_DIR = "FOLDER_PATH";

	@PostMapping("/generatePdf	")
	public ResponseEntity<?> generatePdfAndSendEmail(@RequestBody User user, HttpServletResponse response)
	        throws IOException, DocumentException {
	    // Generate a file name based on the current date and time
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	    String currentDateTime = dateFormat.format(new Date());
	    String fileName = "user_" + currentDateTime + ".pdf";
	    String filePath = PDF_STORAGE_DIR + File.separator + fileName;

	    // Create the PDF file
	    Document document = new Document();
	    File outputFile = new File(filePath);
	    PdfWriter.getInstance(document, new FileOutputStream(outputFile));
	    document.open();
	    document.add(new Paragraph("User Details:"));
	    document.add(new Paragraph("Name: " + user.getName()));
	    document.add(new Paragraph("Email: " + user.getEmail()));
	    document.close();

	    // Send the PDF as an email attachment
	    sendEmailWithAttachment(user.getEmail(), filePath);

	    // Optionally, you can return a response indicating that the email has been sent
	    return ResponseEntity.ok("Email with PDF attachment sent to: " + user.getEmail());
	}

	private void sendEmailWithAttachment(String recipientEmail, String filePath) {
	    // Email configuration
	    final String senderEmail = "youremail@gmail.com";
	    final String senderPassword = "your_app_password"; // Use application-specific password or OAuth

	    // Set up properties for the email server
	    Properties props = new Properties();
	    props.put("mail.smtp.host", "smtp.gmail.com");
	    props.put("mail.smtp.port", "587");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");

	    // Create a session with the email server
	    Session session = Session.getInstance(props, new Authenticator() {
	        protected PasswordAuthentication getPasswordAuthentication() {
	            return new PasswordAuthentication(senderEmail, senderPassword);
	        }
	    });

	    try {
	        // Create a message
	        Message message = new MimeMessage(session);
	        message.setFrom(new InternetAddress(senderEmail));
	        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
	        message.setSubject("User Details PDF");

	        // Create the email body
	        BodyPart messageBodyPart = new MimeBodyPart();
	        messageBodyPart.setText("Please find the attached PDF with user details.");

	        // Create the attachment
	        MimeBodyPart attachmentPart = new MimeBodyPart();
	        DataSource source = new FileDataSource(filePath);
	        attachmentPart.setDataHandler(new DataHandler(source));
	        attachmentPart.setFileName(new File(filePath).getName());

	        // Create the multipart message
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        multipart.addBodyPart(attachmentPart);

	        // Set the multipart content for the message
	        message.setContent(multipart);

	        // Send the email
	        Transport.send(message);
	    } catch (MessagingException e) {
	        e.printStackTrace();
	        // Handle email sending failure
	    }
	}
}
