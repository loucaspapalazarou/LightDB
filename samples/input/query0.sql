-- SELECT * FROM Boats, Sailors, Reserves WHERE Sailors.A=Reserves.G AND Boats.D>Sailors.B AND Reserves.H=10;
-- SELECT * FROM Boats, Sailors, Reserves WHERE Sailors.A=Reserves.G AND Boats.D>Sailors.B;
-- SELECT * FROM Sailors S WHERE S.A < 3;
SELECT B.D FROM Sailors S, Boats B WHERE S.A <= 3 AND B.D > 102;