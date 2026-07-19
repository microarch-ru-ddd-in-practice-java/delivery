package microarch.delivery.core.application.commands;

/**
 * Команда автоматического назначения первого не назначенного заказа на ближайшего свободного курьера.
 */
public final class AssignOrderCommand {

    public static AssignOrderCommand create() {
        return new AssignOrderCommand();
    }
}
