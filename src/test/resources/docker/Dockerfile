FROM jenkins/ssh-agent:7.11.0-jdk21

# Define build argument for ansible-core version
ARG ANSIBLE_CORE_VERSION=

# sshpass used by the plugin
RUN apt-get update && \
    apt-get install -y python3 python3-pip sshpass && \
    rm -rf /var/lib/apt/lists/*

# Install ansible
RUN pip3 install --break-system-packages ansible-core==${ANSIBLE_CORE_VERSION} 

ENV PATH="${PATH}:/root/.local/bin"

# Create ansible files
RUN mkdir -p /ansible && \
    mkdir -p /etc/ansible && \
    mkdir -p /ansible/tmp && \
    chmod 1777 /ansible/tmp && \
    echo "---\nall:\n  hosts:\n    local:\n      ansible_connection: local" > /ansible/inventory.yml && \
    echo "---\n- hosts: local\n  tasks:\n  - debug:\n      msg: 'Hello World'" > /ansible/playbook.yml && \
    echo "[defaults]\ninventory = /ansible/inventory.yml" > /etc/ansible/ansible.cfg

# Test only. Safe to connect with user/password
RUN echo "password\npassword" | passwd root
RUN sed -i 's/PasswordAuthentication no/PasswordAuthentication yes/g' /etc/ssh/sshd_config
RUN sed -i 's/PermitRootLogin no/PermitRootLogin yes/g' /etc/ssh/sshd_config
