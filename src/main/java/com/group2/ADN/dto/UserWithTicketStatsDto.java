package com.group2.ADN.dto;

import com.group2.ADN.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserWithTicketStatsDto {
    private User user;
    private long inProgressTickets;
    private long completedTickets;

    public UserWithTicketStatsDto(User user, long inProgressTickets, long completedTickets) {
        this.user = user;
        this.inProgressTickets = inProgressTickets;
        this.completedTickets = completedTickets;
    }
} 