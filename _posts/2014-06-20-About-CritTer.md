---
title:  "About CritTer2"
---
One of the hardest habits to learn when ﬁrst learning programming is writing stylistically good code.
Badly written code can be difﬁcult to read and debug, which means that teaching good code style is
incredibly important. Unfortunately, manually grading code for stylistic ﬂaws is tedious, repetitive
and error-prone, as many stylistic ﬂaws are small and easy to miss. Automating the task saves a
great deal of time for instructors and provides students with a way to style-check their own code
before submitting assignments.

To ﬁll the void of customizable, automated C style checkers, Erin Rosenbaum created a tool called
CritTer (Critique from the Terminal) in 2011, used in Princeton University’s Introduction
to Programming Systems (COS 217) up until now. Her tool succeeded in providing a radical improvement in
detecting stylistic ﬂaws in C code, but had several issues, including a failure to parse certain
correct C code.

For my Independent Work project at Princeton University, I worked with Dr. Robert Dondero to create
a new version of CritTer which would be free from these issues, using CETUS, a C compiler infrastructure
with an extensive parse tree framework generated from inputted source code. This is the tool presented here:
a completely new implementation of CritTer that is entirely customizable, stable and that matches
the performance of the original tool.
