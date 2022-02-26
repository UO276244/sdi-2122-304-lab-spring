package com.uniovi.notaneitor.services;

import com.uniovi.notaneitor.entities.Mark;
import com.uniovi.notaneitor.entities.User;
import com.uniovi.notaneitor.repositories.MarksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class MarksService {

    @Autowired
    private MarksRepository marksRepository;

    /* Example of Constructor-Based Dependency Injection*/
    private final HttpSession httpSession;

    public MarksService(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public Page<Mark> getMarks(Pageable pageable) {
        Page<Mark> marks =
        marksRepository.findAll(pageable);
        return marks;
    }

    public Mark getMark(Long id) {
        Set<Mark> consultedList = (Set<Mark>) httpSession.getAttribute("consultedList");
        if (consultedList == null) {
            consultedList = new HashSet<Mark>();
        }
        Mark obtainedMark = marksRepository.findById(id).get();

        if (!consultedListContains(consultedList, obtainedMark)) {
            consultedList.add(obtainedMark);
        }

        httpSession.setAttribute("consultedList", consultedList);
        return obtainedMark;

    }


    private boolean consultedListContains(Set<Mark> consulted, Mark current) {

        for (Mark m : consulted) {
            if (m.getId().equals(current.getId())) return true;
        }

        return false;

    }

    public void addMark(Mark mark) {
        // Si en Id es null le asignamos el ultimo + 1 de la lista
        marksRepository.save(mark);
    }

    public void deleteMark(Long id) {
        marksRepository.deleteById(id);
    }

    public void setMarkResend(boolean revised, Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String dni = auth.getName();
        Mark mark = marksRepository.findById(id).get();
        if (mark.getUser().getDni().equals(dni)) {
            marksRepository.updateResend(revised, id);
        }
    }


    public Page<Mark> getMarksForUser(Pageable pageable, User user) {
        Page<Mark> marks = new PageImpl<Mark>(new LinkedList<Mark>());
        if (user.getRole().equals("ROLE_STUDENT")) {
            marks = marksRepository.findAllByUser(pageable, user);
        }
        if (user.getRole().equals("ROLE_PROFESSOR")) {
            marks = getMarks(pageable);
        }
        return marks;
    }

    public Page<Mark> searchMarksByDescriptionAndNameForUser(Pageable pageable,String searchText, User user) {
        Page<Mark> marks = new PageImpl<Mark>(new LinkedList<Mark>());
        searchText = "%" + searchText + "%";
        if (user.getRole().equals("ROLE_STUDENT")) {
            marks = marksRepository.searchByDescriptionNameAndUser(pageable,searchText, user);
        }
        if (user.getRole().equals("ROLE_PROFESSOR")) {
            marks = marksRepository.searchByDescriptionAndName(pageable, searchText);
        }
        return marks;
    }



}
