package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;

public record CreateCourierCommand(String name) {

    public Error validate() {
        return Guard.againstNullOrEmpty(name, "name");
    }
}
