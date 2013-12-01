package com.thoughtworks.bbs.web;

import com.thoughtworks.bbs.model.Post;
import com.thoughtworks.bbs.model.User;
import com.thoughtworks.bbs.service.PostService;
import com.thoughtworks.bbs.service.UserService;
import com.thoughtworks.bbs.service.impl.UserServiceImpl;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class UserControllerTest {
    private UserService userService;
    private PostService postService;
    private UserController userController;
    private HttpServletRequest request;

    private Principal principal;
    private User user;
    private Post post;
    private ModelAndView expected;
    private ModelAndView result;
    private List<Post> postList;

    @Before
    public void setup() {
        userService = mock(UserServiceImpl.class);
        postService = mock(PostService.class);
        principal = mock(Principal.class);
        request = mock(HttpServletRequest.class);

        user = new User();
        user.setUserName("username");
        user.setPasswordHash("123456");

        post = new Post();
        post.setAuthorName("username").setTitle("username").setContent("username");

        postList = new ArrayList<Post>();
        postList.add(post);

        when(principal.getName()).thenReturn("username");
        when(userService.getByUsername("username")).thenReturn(user);
        when(postService.findMainPostByAuthorName("username")).thenReturn(postList);

        userController = new UserController().setUserService(userService).setPostService(postService);
    }

    @Test
    public void shouldJumpToRegisterWhenUrlMatchCreate() {
        result = userController.registerUser(new ModelMap());
        expected = new ModelAndView("user/register");

        assertEquals("Page should jump to user/register page", expected.getViewName(), result.getViewName());
    }

    @Test
    public void shouldJumpToProfileWhenUrlMatchProfile() {
        ModelMap model = new ModelMap();

        result = userController.userProfile(model,principal);
        expected = new ModelAndView("user/profile");

        assertEquals("page should jump to user/profile", expected.getViewName(), result.getViewName());
    }

    @Test
    public void shouldJumpToChangePasswordWhenUrlMatchChangePassword() {

        result = userController.changePassword(new ModelMap(), principal);
        expected = new ModelAndView("user/changePassword");

        assertEquals("page should jump to user/changePassword", expected.getViewName(), result.getViewName());
    }

    @Test
    public void shouldJumpToCreateSuccessAfterCreateNew() {
        Throwable th = null;

        expected = new ModelAndView("user/createSuccess");
        try {
            result = userController.processCreate(request);
        } catch (IOException e) {
            th = e;
        }

        assertEquals("page should jump to user/createSuccess", expected.getViewName(), result.getViewName());
        assertNull("There should be no exceptions.", th);
    }

    @Test
    public void shouldJumpToProfileAfterPasswordChangeSuccess() {
        Throwable th = null;
        ModelMap model = new ModelMap();
        when(request.getParameter("password")).thenReturn("123456");
        when(request.getParameter("confirmPassword")).thenReturn("1234567");
        when(userService.userVerify(user, "123456")).thenReturn(true);
        when(userService.password(user, "1234567")).thenReturn(true);


        expected = new ModelAndView("user/profile");
        try {
            result = userController.processPasswordChange(request, principal, model);
        } catch (IOException e) {
           th = e;
        }
        verify(userService).password(eq(user), argThat(new PasswordMatcher()));
        assertEquals("page should jump to user/profile", expected.getViewName(), result.getViewName());
        assertNull("There should be no exceptions.", th);
    }

    @Test
    public void shouldJumpToProfileAfterPasswordChangeFailed() {
        Throwable th = null;
        ModelMap model = new ModelMap();
        when(request.getParameter("password")).thenReturn("12345678");
        when(request.getParameter("confirmPassword")).thenReturn("1234567");
        when(userService.userVerify(user, "12345678")).thenReturn(false);

        expected = new ModelAndView("user/profile");
        try {
            result = userController.processPasswordChange(request, principal, model);
        } catch (IOException e) {
            th = e;
        }
        verify(userService, times(0)).password(argThat(new UserMatcher()), argThat(new PasswordMatcher()));
        assertEquals("page should jump to user/profile", expected.getViewName(), result.getViewName());
        assertNull("There should be no exceptions.", th);
    }

    @Test
    public void shouldJumpToUpdateProfileWhenUrlMatchUpdateUsername() {

        result = userController.changeUsername(new ModelMap(), principal);
        expected = new ModelAndView("user/updateProfile");

        assertEquals("page should jump to user/updateProfile", expected.getViewName(), result.getViewName());
    }

    private class PasswordMatcher extends ArgumentMatcher<String> {

        @Override
        public boolean matches(Object item) {
            if(item != null && item instanceof String)
                return true;
            return false;
        }
    }

    private class UserMatcher extends ArgumentMatcher<User> {

        @Override
        public boolean matches(Object arg) {
            if( null == arg || !(arg instanceof User) )
                return  false;
            return ((User) arg).getUserName().equals("username");
        }
    }
}
