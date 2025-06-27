package kr.co.yournews.admin.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String usersPage() {
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetailsPage(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        return "admin/user-detail";
    }

    @GetMapping("/posts")
    public String postPage() {
        return "admin/posts";
    }

    @GetMapping("/posts/create")
    public String postCreatePage() {
        return "admin/post-create";
    }

    @GetMapping("/posts/{id}")
    public String postDetailsPage(@PathVariable Long id, Model model) {
        model.addAttribute("postId", id);
        return "admin/post-detail";
    }

    @GetMapping("/posts/{id}/edit")
    public String postEditPage(@PathVariable Long id, Model model) {
        model.addAttribute("postId", id);
        return "admin/post-edit";
    }

    @GetMapping("/news")
    public String newsPage() {
        return "admin/news";
    }

    @GetMapping("/news/{id}")
    public String newsDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("news", id);
        return "admin/news-detail";
    }

    @GetMapping("/news/create")
    public String newsCreatePage() {
        return "admin/news-create";
    }
}
