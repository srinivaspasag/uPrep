package com.lms.board.pojos.requests;

import com.lms.common.vedantu.enums.GradeType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
public class UploadConsumerBoardReq extends UploadGlobalBoardReq {
	@NotBlank(message = "treeName must not be empty")
	public String treeName;
	@NotNull
	public Set<GradeType> grades;

	public UploadConsumerBoardReq(MultipartFile body) {
		super(body);
		// TODO Auto-generated constructor stub
	}

}
