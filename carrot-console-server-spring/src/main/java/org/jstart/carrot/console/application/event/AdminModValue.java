package org.jstart.carrot.console.application.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
@ToString
public class AdminModValue extends ApplicationEvent {

    private Integer mod;

    private Integer modValue;

    public AdminModValue(Object source,
                         Integer mod,
                         Integer modValue

    ) {
        super(source);
        this.mod = mod;
        this.modValue = modValue;
    }
}
