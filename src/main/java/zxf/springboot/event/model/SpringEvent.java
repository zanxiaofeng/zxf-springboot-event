package zxf.springboot.event.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;

@Data
@AllArgsConstructor
public class SpringEvent {
    @Serial
    private static final long serial = 1l;
    private String data;
}
