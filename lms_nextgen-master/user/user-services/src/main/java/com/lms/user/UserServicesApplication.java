package com.lms.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.lms"})
public class UserServicesApplication/* implements ApplicationRunner*/ {

	public static void main(String[] args) {
		SpringApplication.run(UserServicesApplication.class, args);
	}

	/*@Autowired
	private EmailService emailService;

	@Override
	public void run(ApplicationArguments args) throws Exception {

		Mail mail = new Mail();
		mail.setFrom("dm@learnpedia.in");//replace with your desired email
		mail.setMailTo("sainath.pabba@thrymr.net");//replace with your desired email
		mail.setSubject("Email with Spring boot and thymeleaf template!");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("name", "Developer!");
		model.put("location", "India");
		model.put("sign", "Java Developer");
		mail.setProps(model);
		emailService.sendEmail(mail);
	}*/
}
