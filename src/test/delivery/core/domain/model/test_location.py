import pytest

from delivery.core.domain.model.location import Location

def test_valid_location():
    location = Location(x=3, y=1)
    assert location.x == 3
    assert location.y == 1

@pytest.mark.parametrize(
    "x, y",
    [
        (-1, 3),
        (11, 4),
        (6, -2),
        (2, 13),
    ],
)
def test_invalid_coordinate(x, y):
    with pytest.raises(ValueError):
        Location(x=x, y=y)

def test_distance_calculation():
    location_1 = Location(1, 4)
    location_2 = Location(8, 1)
    expected = 7 + 3

    assert location_1.distance_to(location_2) == expected
    assert location_2.distance_to(location_1) == expected

def test_immutability():
    location = Location(x=1, y=2)

    with pytest.raises(AttributeError):
        location.x = 5

    with pytest.raises(AttributeError):
        location.y = 3

@pytest.mark.parametrize(
    "x1, y1, x2, y2, expected",
    [
        (1, 3, 1, 3, True),
        (1, 3, 3, 1, False),
        (1, 3, 2, 9, False),
    ],
)
def test_equal_both_location(x1, y1, x2, y2, expected):
    assert (Location(x1, y1) == Location(x2, y2)) == expected

def test_equal_one_is_not_location():
    assert not (1 == Location(1, 3))
