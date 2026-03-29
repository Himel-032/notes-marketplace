package com.notes.notesmarketplace.dto.admin;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AdminAnalyticsDto {
    long totalUsers;
    long totalNotes;
    double totalSales;
}
