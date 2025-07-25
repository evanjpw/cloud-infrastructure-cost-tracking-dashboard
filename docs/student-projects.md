# Using the Cloud Billing Dashboard for Education

I'd like to be able to use this dashboard as a way to teach, & to test, students.

These students will be trying to learn to be cloud engineers, SREs, & DevOps engineers.

I want to be able to teach them about how cloud service billing works, & how to optimize costs.

One crucial element of this is how to test their knowledge. How can we grade them? How can we create scenarios?

Here's a description of the kind of testing I'm thinking of.

## Student Testing: Cloud Cost Optimization Analysis

Students will be asked to Analyze cloud resource usage and identify cost optimization opportunities.

They will be provided with dashboard that shows Cloud billing data resource utilization metrics for a simulated cloud environment.
The environment will include various resources such as virtual machines, storage,
Lambda serverless functions, load balancers, Kubernetes clusters, VPCs, Virtual Networks, & databases.

**NICE TO HAVE** - The abillity to also show them architecture diagrams of these resources,
so they can see how they are connected and understand the overall architecture.

### Expectations

Students will produce a Cost optimization report with specific recommendations and projected savings.

### Grading Criteria

- Cost drivers identified correctly.
- Optimization recommendations viable.
- Savings projections realistic.
- Implementation priority logical.

### Testing Environment

We would use our dashboard as the tool for students to use for the Analysis.

We would want the dashboard to be as representative as possible of real cloud billing consoles, while being
platform neutral so that we were not teaching AWS or Azure or GCP specifically.

**NICE TO HAVE** (low priority) In future, it would be helpful to also be able to imitate specific cloud
billing consoles of AWS, Azure, GCP, or any similar platforms.

It is imperative that we can quickly setup realistic scenarios for these tests. We'd prefer to be able to generate them, either with a conventional program or using an LLM. However, the scenarios must be real
world setups, & there must be some sensible way for them to meet the objectives. The expectation would be
that the scenario would provide about the same level of possible optimization, & have about the same
complexity each time, but have sufficient variety that students will not be expecting the same thing
each time (they should be able to take the test 50 times & have 50 very different challenges).

**NICE TO HAVE** A setable scale of complexity, difficulty & avialable optimizations.

It is also crucial that the grader be provided clear information for grading. They will need to see the all
of the data that the student can see, & any data that we don't show them. To make grading more objective,
it would also help if any hints from the generation process like "we're adding a higher number of servers
to the ECS group than they need", or "These individual EC2 instances could be a scaling group" be provided
to the grader. The most optimal setup is where the grading could be done by a machine (algorithm or LLM).

We will also be evaluating the graders (human & machine) to see how we can improve the process & the
quality.
