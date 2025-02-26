package org.example.expert.domain.user.service;

import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {

    @Mock
    private UserService userService;
    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void changeUserRole에서_유저의_권한을_정상적으로_수정할_수_있는가() {
        // given
        long userId = 1L;
        User user = new User(userId, "email@email.com", "password", UserRole.USER);
        UserRole userRole = UserRole.ADMIN;
        UserRoleChangeRequest dto = new UserRoleChangeRequest(userRole.name());

        given(userService.findUserByIdOrElseThrow(anyLong())).willReturn(user);

        // when
        userAdminService.changeUserRole(userId, dto);

        // then
        assertThat(user.getUserRole()).isEqualTo(userRole);
    }
}
