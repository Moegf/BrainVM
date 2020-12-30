/**
 * virtualmachine is a package that contains interfaces and classes to run programs written in the esolang <a href="https://en.wikipedia.org/wiki/Brainfuck">BrainF</a>, which has the following six commands and has an "infinite" tape (guaranteed up to the capacity of the java heap):
 * + - increment the value at the current cell
 * - - decrement the value at the current cell
 * > - move one cell to the right
 * < - move one cell to the left
 * . - print out the value at the current cell
 * , - read on byte from input into the current cell
 */
package virtualmachine;