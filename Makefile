CC          = g++
CFLAGS      = -Wall -ansi -pedantic -ggdb
OBJS        = player.o board.o
PLAYERNAME  = player

all: $(PLAYERNAME) testgame
	
$(PLAYERNAME): $(OBJS) wrapper.o
	$(CC) -o $@ $^

testgame: testgame.o
	$(CC) -o $@ $^

testminimax: $(OBJS) testminimax.o
	$(CC) -o $@ $^

%.o: %.cpp
	$(CC) -c $(CFLAGS) -x c++ $< -o $@
	
java:
	make -C java/

cleanjava:
	make -C java/ clean

clean:
	rm -f *.o $(PLAYERNAME) testgame testminimax
	
.PHONY: java testminimax

ai:
	javac -d ./ -cp AI/OthelloAI/src/ AI/OthelloAI/src/net/thenumenorean/othelloai/*.java

aiclean:
	rm -rf ./net/thenumenorean/othelloai
