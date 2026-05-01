from dataclasses import dataclass
from typing import Self

@dataclass(
    frozen=True,
    slots=True
)
class Location:
    x: int
    y: int

    def __post_init__(self) -> None:
        for name, value in (("x", self.x), ("y", self.y)):
            self._validate_coordinate(name, value)

    @staticmethod
    def _validate_coordinate(coordinate_name: str, coordinate_value: int) -> None:
        if not (1 <= coordinate_value <= 10):
            raise ValueError(f"{coordinate_name} must be between 1 and 10, got {coordinate_value}")

    def distance_to(self, other: Self) -> int:
        return abs(self.x - other.x) + abs(self.y - other.y)

