-- SELECT * FROM Sailors WHERE Sailors.B >= 200;
-- SELECT * FROM Sailors, Boats, Reserves;
-- SELECT * FROM Sailors, Reserves WHERE Sailors.A = Reserves.G;
SELECT * FROM Sailors, Reserves WHERE Sailors.A > 0;