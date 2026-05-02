import uuid


class InvalidName(ValueError):
    pass


class InvalidTotalVolume(ValueError):
    pass


class InvalidOrderId(ValueError):
    pass


class StorageError(ValueError):
    pass


class StoragePlace:
    _initialized: bool = False

    def __init__(
        self,
        name: str,
        total_volume: float | int,
        order_id: uuid.UUID | None = None,
    ):
        self._name = self._validate_name(name)
        self._total_volume = self._validate_volume(total_volume)
        self._order_id = self._validate_order_id(order_id)
        self._id = uuid.uuid4()

        # Initialization is done, freeze direct updates.
        object.__setattr__(self, "_initialized", True)

    @staticmethod
    def _validate_name(name: str) -> str:
        if not isinstance(name, str):
            raise InvalidName(f"`name` should be str, found {type(name)}!")
        if len(name) == 0:
            raise InvalidName(f"`name` should be non-empty, got {len(name)}!")
        return name

    @staticmethod
    def _validate_volume(volume: float) -> float:
        if isinstance(volume, int):
            volume = float(volume)
        if not isinstance(volume, float):
            raise InvalidTotalVolume(
                f"`total_volume` should be float, found {type(volume)}!"
            )
        if volume <= 0:
            raise InvalidTotalVolume(
                f"`total_volume` should be greater than 0, found {volume}!"
            )

        return volume

    @staticmethod
    def _validate_order_id(order_id: uuid.UUID | None) -> uuid.UUID | None:
        if order_id is not None and not isinstance(order_id, uuid.UUID):
            raise InvalidOrderId(
                f"`order_id` should be UUID or None, found {type(order_id)}"
            )
        return order_id

    @property
    def id(self) -> uuid.UUID:
        return self._id

    @property
    def name(self) -> str:
        return self._name

    @name.setter
    def name(self, new_value: str) -> None:
        object.__setattr__(self, "_name", self._validate_name(new_value))

    @property
    def total_volume(self) -> float:
        return self._total_volume

    @total_volume.setter
    def total_volume(self, new_value: float | int) -> None:
        object.__setattr__(self, "_total_volume", self._validate_volume(new_value))

    @property
    def order_id(self) -> uuid.UUID | None:
        return self._order_id

    def _set_order_id(
        self,
        order_id: uuid.UUID | None,
    ) -> None:
        object.__setattr__(self, "_order_id", self._validate_order_id(order_id))

    def __setattr__(self, key: str, value: object) -> None:
        # if there's a property with a setter on the class, use it
        cls_attr = getattr(type(self), key, None)
        if isinstance(cls_attr, property) and cls_attr.fset is not None:
            cls_attr.fset(self, value)
            return

        # initialization phase: allow anything
        if not getattr(self, "_initialized", False):
            super().__setattr__(key, value)
            return

        # post-init: block direct underscore writes
        raise AttributeError(
            f"{type(self).__name__} fields cannot be assigned directly; "
            f"use the public properties."
        )

    def put_order(
        self,
        order_id: uuid.UUID,
        order_volume: int | float,
    ) -> None:
        if not self.can_fit(order_volume):
            raise StorageError(
                "Can't fit order: either storage is occupied or the item is too big!"
            )
        self._set_order_id(order_id)

    def extract_order(
        self,
    ) -> None:
        if not self._is_occupied():
            raise StorageError("Storage is already empty")

        self._set_order_id(None)

    def can_fit(self, order_volume: int | float) -> bool:
        self._validate_volume(order_volume)

        if self._is_occupied():
            return False

        return order_volume <= self.total_volume

    def _is_occupied(self):
        return self.order_id is not None
