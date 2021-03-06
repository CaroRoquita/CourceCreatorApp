package com.finalproject.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.finalproject.domain.Course;
import com.finalproject.domain.Lesson;
import com.finalproject.domain.Section;
import com.finalproject.domain.User;
import com.finalproject.repository.CourseRepository;
import com.finalproject.repository.SectionRepository;
import com.finalproject.repository.UserRepository;

@Controller
public class CourseController
{
  private CourseRepository courseRepo;
  private SectionRepository sectionRepo;
  private UserRepository userRepo;
  
  @RequestMapping("/")
  public String rootPath ()
  {
    return "redirect:/courses";
  }
  
  @RequestMapping(value="courses", method=RequestMethod.GET)
  public String courses (ModelMap model)
  {
    
    List<Course> courses = courseRepo.findAll();
    model.put("courses", courses);
    Course course = new Course();
    model.put("course", course);
    
    return "courses";
  }
  
  @RequestMapping(value="courses", method=RequestMethod.POST)
  public String coursesPost (@ModelAttribute Course course, ModelMap model, @AuthenticationPrincipal User user)
  {
    course.setUser(user);
    user.getCourses().add(course);
    
    Course savedCourse = courseRepo.save(course);
    
    
    return "redirect:/editCourse/" + savedCourse.getId();
  }
  
  @RequestMapping(value="editCourse/{courseId}", method=RequestMethod.GET)
  public String editCourseGet (@PathVariable Long courseId, ModelMap model)
  {
    Course course = courseRepo.findOne(courseId);
    if (course == null)
      return "redirect:/";
    model.put("course", course);
    return "editCourse";
  }

  @RequestMapping(value="editCourse/{courseId}/deleteCourse", method=RequestMethod.POST)
  public String deletCourse (@PathVariable Long courseId, @AuthenticationPrincipal User user)
  {
    Course course = courseRepo.findOne(courseId);
    User savedUser = userRepo.findUserByEmail(user.getEmail());
    
    savedUser.getCourses().remove(course);
    
    courseRepo.delete(course);
    
    return "redirect:/";
  }
  
  @RequestMapping(value="editCourse/createSection", method=RequestMethod.POST)
  public @ResponseBody Course createSection (@RequestParam Long courseId, @RequestParam String sectionName)
  {
    Course course = courseRepo.findOne(courseId);
    Section section = new Section();
    section.setCourse(course);
    section.setName(sectionName);
    course.getSections().add(section);
    courseRepo.save(course);
    return course;
  }
  
  @RequestMapping(value="editCourse/createLesson", method=RequestMethod.POST)
  public @ResponseBody Course createLesson (@RequestParam Long courseId, 
      @RequestParam Long sectionId,
      @RequestParam String lessonTitle,
      @RequestParam Integer lessonNumber)
  {
    Course course = courseRepo.findOne(courseId);
    for (Section section : course.getSections())
    {
      if (section.getId().equals(sectionId))
      {
        Lesson lesson = new Lesson();
        lesson.setNumber(lessonNumber);
        lesson.setTitle(lessonTitle);
        lesson.setSection(section);
        section.getLessons().add(lesson);
        sectionRepo.save(section);
        break;
      }
    }
    return course;
  }
  
  @Autowired
  public void setCourseRepo(CourseRepository courseRepo)
  {
    this.courseRepo = courseRepo;
  }
  @Autowired
  public void setSectionRepo(SectionRepository sectionRepo)
  {
    this.sectionRepo = sectionRepo;
  }
  @Autowired
  public void setUserRepo(UserRepository userRepo)
  {
    this.userRepo = userRepo;
  }
  
}
