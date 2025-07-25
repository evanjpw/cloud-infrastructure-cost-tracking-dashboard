package com.dashboard.cloud_cost_dashboard.controller;

import com.dashboard.cloud_cost_dashboard.model.Team;
import com.dashboard.cloud_cost_dashboard.repository.TeamRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:3000")
public class TeamsController {

    @Autowired private TeamRepository teamRepository;

    @GetMapping
    public List<Team> getAllTeams() {
        System.out.println("Fetching all teams");
        List<Team> teams = teamRepository.findAll();
        System.out.println("Found " + teams.size() + " teams");
        return teams;
    }
}
