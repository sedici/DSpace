# Display banner
if [ -f "$HOME/.banner" ]; then
   cat "$HOME/.banner"
fi

# Display build message
if [ -f "$HOME/.built" ]; then
   cat "$HOME/.built"
fi

export PS1="\[\e[32;1m\]\u@\w > \[\e[0m\]"
