package com.makersacademy.acebook.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import com.makersacademy.acebook.lib.ImageUtil;
import com.makersacademy.acebook.model.Like;
import com.makersacademy.acebook.model.Post;
import com.makersacademy.acebook.model.User;
import com.makersacademy.acebook.model.Comment;
import com.makersacademy.acebook.repository.PostRepository;
import com.makersacademy.acebook.repository.UserRepository;
import com.makersacademy.acebook.repository.CommentRepository;
import com.makersacademy.acebook.repository.LikeRepository;

//import org.omg.CORBA.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class PostsController {

    @Autowired
    PostRepository repository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    LikeRepository likeRepository;


    @GetMapping("/posts")
    public String index(Model model) throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User thisUsers = userRepository.findByUsername(username).get(0);
        Iterable<Post> posts = repository.findAll(Sort.by(Sort.Direction.DESC, "time"));

        model.addAttribute("thisUser", thisUsers);
        model.addAttribute("imgUtil", new ImageUtil());
        model.addAttribute("posts", posts);
        model.addAttribute("post", new Post());
        model.addAttribute("like", new Like(0));
        return "posts/index";
    }

    @PostMapping("/posts")
    public RedirectView create(@ModelAttribute Post post, @RequestParam("file") MultipartFile file) throws IOException {
        post.contentimage = file.getBytes();

        repository.save(post);
        return new RedirectView("/posts");
    }

    @PostMapping("/deletePost/{id}")
    public RedirectView deletePost(@PathVariable Long id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User thisUser = userRepository.findByUsername(username).get(0);
        Post thisPost = repository.findById(id).get();
        List<Comment> comments = commentRepository.findAllByPostId(id);
        List<Like> likes = likeRepository.findAllByPostId(id);

        if (thisPost.user.getId() == thisUser.getId()) {
            commentRepository.deleteAll(comments);
            likeRepository.deleteAll(likes);
            repository.deleteById(thisPost.getId());
            
        }


        return new RedirectView("/posts");
    }

    @GetMapping("/post/{id}")
    public String post(@PathVariable Long id, Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User user = userRepository.findByUsername(username).get(0);
        Post post = repository.findById(id).get();
        List<Comment> comments = commentRepository.findByPostId(id);
        model.addAttribute("imgUtil", new ImageUtil());
        model.addAttribute("user", user);
        model.addAttribute("comments", comments);
        model.addAttribute("post", post);
        model.addAttribute("comment", new Comment());
        return "posts/post";
    }


    @PostMapping("/post/{id}")
    public RedirectView create(@PathVariable Long id, @ModelAttribute Comment comment) {
        commentRepository.save(comment);
        return new RedirectView("/post/{id}");
    }

    @GetMapping("/edit/{id}")
    public String getEdit(@PathVariable Long id, Model model) {
        Post post = repository.findById(id).get();
        model.addAttribute("post", post);
        return "posts/edit"; // if it's just /edit, it can't find it
    }


    @PostMapping("/edit")
    public RedirectView post(@ModelAttribute Post post) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User thisUser = userRepository.findByUsername(username).get(0);
        if (post.user.getId() == thisUser.getId()) {
            repository.save(post);
        }
        return new RedirectView("/posts");
    }

    @PostMapping("/deleteComment/{id}")
    public RedirectView deleteComment(@PathVariable Long id) {
        Comment comment = commentRepository.findById(id).get();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        User thisUser = userRepository.findByUsername(username).get(0);
        Long postId = comment.post.getId();
        if (comment.user.getId() == thisUser.getId()) {
            commentRepository.deleteById(id);
        }
        return new RedirectView("/post/" + postId.toString());
    }
}
