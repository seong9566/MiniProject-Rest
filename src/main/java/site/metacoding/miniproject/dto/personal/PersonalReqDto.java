package site.metacoding.miniproject.dto.personal;

import lombok.Getter;
import lombok.Setter;
import site.metacoding.miniproject.domain.personal.Personal;
import site.metacoding.miniproject.domain.users.Users;

public class PersonalReqDto {
        @Getter
        @Setter
        public static class PersonalJoinDto {
            private String loginId;
            private String loginPassword;
            private String personalName;
            private String personalEmail;
            private String personalPhoneNumber;
            private String personalEducation;
            private String personalAddress;

        // Users계정 생성용
        private Integer personalId;

        public Personal personalJoinDtoToPersonalEntity() {
            return Personal.builder()
                .personalName(personalName)
                .personalEmail(personalEmail)
                .personalEducation(personalEducation)
                .personalAddress(personalAddress)
                .personalPhoneNumber(personalPhoneNumber)
                .build();
        }
        
        public Users personalJoinDtoToUserEntity() {
            return Users.builder()
                .loginId(loginId)
                .loginPassword(loginPassword)
                .personalId(personalId)
                .build();
        }
        }

}
