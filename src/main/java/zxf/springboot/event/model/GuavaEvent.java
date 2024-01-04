package zxf.springboot.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class GuavaEvent implements Serializable {
    @Serial
    private static final long serial=1l;
    private String data;
}
