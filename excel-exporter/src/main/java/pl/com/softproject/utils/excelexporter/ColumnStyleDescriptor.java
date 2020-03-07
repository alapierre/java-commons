package pl.com.softproject.utils.excelexporter;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class ColumnStyleDescriptor
 *
 * @author Mateusz Mądry <mmadry@soft-project.pl>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnStyleDescriptor implements Serializable {

    private String excelFormatMask;
    private ColumnStyleType type;

}
