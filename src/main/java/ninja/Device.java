package ninja;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** A device associated with a customer. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Device {

    public static enum Type {
        WINDOWS_WORKSTATION,
        WINDOWS_SERVER,
        MAC;
    }

    /** Device ID. */
    @NotNull
    @Size(min = 1, max = 45)
    private String id;

    /** Device type, used to determine service pricing. */
    @NotNull
    private Type type;

    /** Device name. */
    @NotNull
    @Size(min = 1, max = 45)
    private String name;
}
