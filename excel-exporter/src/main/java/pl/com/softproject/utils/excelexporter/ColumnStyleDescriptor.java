package pl.com.softproject.utils.excelexporter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Class ColumnStyleDescriptor
 *
 * @author Mateusz Mądry {@literal <mmadry@soft-project.pl>}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnStyleDescriptor implements Serializable {

    private String excelFormatMask;
    private ColumnStyleType type;

}
