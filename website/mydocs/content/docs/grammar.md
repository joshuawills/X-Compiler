---
title: "Grammar"
weight: 1
---

This is a CFG for the X programming language. It does NOT specify semantic rules, those are loosely
defined in other language documentation.

{{< katex display >}}
\begin{align}
\textit{program} &\to \textit{(using-stmts || import-stmts)}^* (\textit{function }|\textit{ global-var }|\textit{ enum }|\textit{ struct})^*  \\ \\

\textit{import-stmts} &\to \textbf{import } \textit{STRINGLITERAL} \textbf{ as } \textit{ident} \text{";"} \\ 
\textit{import-stmts} &\to \textbf{import } \textit{ident} (\text{"," } \textit{ident})? \text{";"} \\
\textit{using-stmts} &\to \textbf{using } \textit{STRINGLITERAL} \text{";"} \\
\textit{using-stmts} &\to \textbf{using } \textit{ident } (\text{"," } \textit{ident})^* \text{ ";"} \\ \\

\textit{global-var} &\to \textbf{export}? \textbf{ let } \textbf{mut}? \textit{ ident } (\text{ ":" type})? (\text{"="} expr)? \text{";"}\\
\textit{local-var} &\to \textbf{let } \textbf{mut}? \textit{ ident } (\text{ ":" type})? (\text{"="} expr)? \text{";"}\\
\textit{tuple-destructure} &\to \textbf{let } \textbf{mut}? \textit{ ident } ("," \textit{ident})? (\text{ ":" type})? \text{"="} expr \text{ ";"}\\
\textit{function} &\to \textbf{export}? \textbf{ fn } (\textit{"("} \textit{"mut"}?\textit{ ident } \textit{":"} \textit{ type }\textit{")"})? (\text{"@"})? \textit{ ident} \text{ "\\("} \textit{ para-list} \text{ "\\)"} \text{ "->"} \textit{compound-stmt }\\
\textit{enum} &\to \textbf{export}? \textbf{ enum} \textit{ ident } \textit{"->" "\{ "} \textit{ident} (\textit{"," ident})^*\textit{ " \}"}\\
\textit{struct} &\to \textbf{export}? \textbf{ struct} \textit{ ident } \textit{"->" "\{ "} \textbf{mut}? \textit{ ident}\text{ ":" type } (\textit{"," \textbf{mut}? \textit{ ident}\text{ ":" type }})^*\textit{ " \}"}\\ \\

\text{compound-stmt} &\to \text{"\\\{" } (\textit{stmt } | \textit{ local-var} | \textit{ tuple-destructure})^* \text{ "\\\}"} \\
\text{stmt} &\to
\begin{cases}
\textit{compound-stmt} \\
\textit{if-stmt} \\
\textit{for-stmt} \\
\textit{while-stmt} \\
\textit{do-while-stmt} \\
\textit{break-stmt} \\
\textit{continue-stmt} \\
\textit{return-stmt} \\
\textit{loop-stmt} \\
\textit{expr-stmt} \\
\end{cases} \\
\textit{if-stmt} &\to \textbf{if} \textit{ expr stmt } (\textbf{else if } \textit{expr stmt})^* (\textbf{else} \textit{
stmt})? \\
\textit{for-stmt} &\to \textbf{for} \textit{ expr?} \text{ ";"} \textit{ expr?} \text{ ";"} \textit{ expr? stmt} \\
\textit{while-stmt} &\to \textbf{while} \textit{ expr stmt} \\
\textit{do-while-stmt} &\to \textbf{do} \textit{ compound-stmt expr} \\
\textit{break-stmt} &\to \textbf{break} \text{ ";"} \\
\textit{continue-stmt} &\to \textbf{continue } \text{ ";"} \\
\textit{return-stmt} &\to \textbf{return} \textit{ expr}? \text{ ";"} \\
\textit{expr-stmt} &\to \textit{expr} \text{ ";"} \\
\textit{loop-stmt} &\to \textbf{ loop} ( \textit{ident } \textbf{in})? \textit{ INTLITERAL?} \textit{ INTLITERAL?} \textit{ compound-stmt}\\
\textit{loop-stmt} &\to \textbf{ loop} \textit{ ident } \textit{ compound-stmt}\\ \\

\textit{expr} &\to \textit{assignment-expr } (\textbf{as} \textit{ type})? || \textit{size-of-expr} || \textit{type-expr} || \textit{tuple-expr} \\
\textit{tuple-expr} &\to \text{"( "} expr (\text{"," } expr)?\text{" )"}\\
\textit{size-of-expr} &\to \textbf{size} ( \textit{type | IDENT} ) \\ 
\textit{type-expr} &\to \textbf{type} ( \textit{expr} ) \\ 
\textit{type-expr} &\to \textbf{size} ( \textit{type | IDENT} ) \\ 
\textit{assignment-expr} &\to \textit{or-expr } || \textit{unary-expr} \textbf{ ASSIGNMENT-OPERATOR } \textit{assignment-expr}\\  
\textit{or-expr} &\to \textit{and-expr } (\text{"||" } \textit{and-expr})^* \\
\textit{and-expr} &\to \textit{equality-expr } (\text{"\\\&\\\&" } \textit{equality-expr})^* \\
\textit{equality-expr} &\to \textit{relational-expr } ((\text{"==" } | \text{ "!=" }) \textit{ relational-expr})^* \\
\textit{relational-expr} &\to \textit{additive-expr } ((\text{"<" } | \text{ "<=" } | \text{ ">" } | \text{ ">=" })
\textit{ additive-expr})^* \\
\textit{additive-expr} &\to \textit{mult-expr } ((\text{"-" } | \text{ "+" }) \textit{ mult-expr})^* \\
\textit{mult-expr} &\to \textit{unary-expr } ((\text{"\%" } | \text{"*" } | \text{ "/" }) \textit{ unary-expr})^* \\
\textit{unary-expr} &\to
\begin{cases}
\textit{size-of-expr} \\
\textbf{INTLITERAL} \\
\textbf{FLOATLITERAL} \\
\textbf{STRINGLITERAL} \\
\textbf{BOOLLITERAL} \\
\textbf{CHARLITERAL} \\
\textit{null} \\
"(" \textit{ expr } ")" \\
\textit{ident} \\
\textit{(+ | - | ! | * | \&) unary-expr} \\
\textit{array-init-expr} \\
\textit{postfix-expression} \\
\end{cases} \\
\\

\textit{postfix-expression} &\to
\begin{cases}
\textit{func-call} \\
\textit{array-index} \\
\textit{enum-expr} \\
\textit{struct-expr} \\
\end{cases} \\
\\

\textit{array-index} &\to \textit{ident } "[" \textit{expr} "]" \\
\textit{enum-expr} &\to \textit{ident } \text{ "." }\textit{ident} \\
\textit{struct-expr} &\to \textit{ident } \textit{"\{ " } \textit{struct-args} \textit{ "\} "} \\ 
\textit{func-call} &\to \textit{ident } "(" \textit{args} ")" \\
\textit{array-init-expr} &\to \textit{"["} (\textit{expr } (\text{","} \textit{ expr})^*)? \textit{"]"} \\
\textit{args} &\to \textit{expr } (\text{","} \textit{ expr})^* \text{ | } \epsilon\\
\textit{struct-args} &\to \textit{expr } (\text{","} \textit{ expr})^* \text{ | } \epsilon\\
\textit{para-list} &\to \textit{arg } (\textit{"," arg})^*  \text{ | } \epsilon \\ \\
\textit{arg} &\to \textbf{mut}? \textit{ ident } \text{ ":" } \textit{ type}\\

\textit{ident} &\to \textbf{letter} (\textbf{letter } | \textbf{ digit})^* || \textit{ \$} \\
\textit{type} &\to \textbf{ident u8 | u32 | u64 | i8 | i32 | i64 | void | bool | f32 | f64 | \textit{type}* | \textit{type}[\textit{INTLITERAL}] } \\

\textit{INTLITERAL} &\to [0-9]+ \\
\textit{FLOATLITERAL} &\to [0-9]^+\textit{"."}[0-9]? \\
\textit{STRINGLITERAL} &\to \textit{"} \textit{ident} \textit{"} \\
\textit{BOOLLITERAL} &\to true | false \\
\textit{CHARLITERAL} &\to \textit{'ident'} \\
\textit{ASSIGNMENT-OPERATOR} &\to \textbf{ "==" "+=" "-=" "*=" "/=" }\\

\end{align}
{{< /katex >}}