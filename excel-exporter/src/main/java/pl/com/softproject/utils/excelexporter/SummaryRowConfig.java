package pl.com.softproject.utils.excelexporter;

import lombok.Builder;
import lombok.Getter;

/**
 * Configuration class for creating summary rows in Excel exports.
 * Uses the builder pattern to allow setting multiple optional parameters.
 */
@Getter
@Builder
public class SummaryRowConfig {
    
    /**
     * Text label to show in the summary row (defaults to "Total:")
     */
    @Builder.Default
    private String summaryLabel = "Total:";
    
    /**
     * Number of rows to skip from the beginning when calculating the sum (defaults to 0)
     */
    @Builder.Default
    private int skipFirstRows = 1;
    
    /**
     * Column index where to put the summary label (defaults to 0, first column)
     */
    @Builder.Default
    private int labelColumnIndex = 0;
    
    @Builder.Default
    private boolean boldText = false;
} 
