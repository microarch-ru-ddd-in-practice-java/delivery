from typing import Self

class Location:
    def __init__(
        self,
        x: int,
        y: int,
    ):
        self._x = self._validate_coordinate(x)
        self._y = self._validate_coordinate(y)

    @property
    def x(self) -> int:
        return self._x

    @property
    def y(self) -> int:
        return self._y

    @staticmethod
    def _validate_coordinate(coordinate: int) -> int:
        if not (1 <= coordinate <= 10):
            raise ValueError("Coordinate should be in between 0 and 10!")

        return coordinate

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Location):
            return False

        return self.x == other.x and self.y == other.y

    def distance_to(self, other: Self) -> int:
        return abs(self.x - other.x) + abs(self.y - other.y)

