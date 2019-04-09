echo %date%
set d=%date:~0,4%-%date:~5,2%-%date:~8,2%
echo %time%
set t=%time:~0,2%-%time:~3,2%-%time:~6,2%
set t=%t: =0%
echo %d%_%t%
git archive -o LiteNote_%d%_%t%.zip --worktree-attributes HEAD
7z d LiteNote_%d%_%t%.zip app/preferred/*

