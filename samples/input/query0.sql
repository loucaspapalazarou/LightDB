-- SELECT * FROM Sailors S, Boats B, Reserves R WHERE S.A=R.G AND B.D>S.B;
SELECT * FROM Sailors, Boats, Reserves WHERE Sailors.A=Reserves.G AND Boats.D>Sailors.B;
